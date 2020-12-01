"""gRPC service registry module."""

import abc
import os

import six
from kazoo.client import KazooClient
from kazoo.retry import KazooRetry
from opncom.logger import logger
import py_eureka_client.eureka_client as eureka_client
from grpccloud.eureka.client import EtcdClient
from grpccloud.nameresolver.address import PlainAddress, JsonAddress


class ServiceRegistry(six.with_metaclass(abc.ABCMeta)):
    """A service registry."""

    @abc.abstractmethod
    def register(self, service_name, service_addr, service_ttl):
        """Register services with the same address."""
        raise NotImplementedError

    @abc.abstractmethod
    def heartbeat(self, service_addr=None):
        """Service registry heartbeat."""
        raise NotImplementedError

    @abc.abstractmethod
    def unregister(self, service_name, service_addr):
        """Unregister services with the same address."""
        raise NotImplementedError


class EtcdServiceRegistry(ServiceRegistry):
    """gRPC service registry based on etcd."""

    def __init__(self, etcd_host=None, etcd_port=None, etcd_client=None):
        """Initialize etcd service registry.

        :param etcd_host: (optional) etcd node host for :class:`client.EtcdClient`.
        :param etcd_port: (optional) etcd node port for :class:`client.EtcdClient`.
        :param etcd_client: (optional) A :class:`client.EtcdClient` object.

        """
        self._client = etcd_client if etcd_client else EtcdClient(
            etcd_host, etcd_port)
        self._leases = {}
        self._services = {}

    def get_lease(self, service_addr, service_ttl):
        """Get a gRPC service lease from etcd.

        :param service_addr: gRPC service address.
        :param service_ttl: gRPC service lease ttl(seconds).
        :rtype `etcd3.lease.Lease`

        """
        lease = self._leases.get(service_addr)
        if lease and lease.remaining_ttl > 0:
            return lease

        lease_id = hash(service_addr)
        lease = self._client.lease(service_ttl, lease_id)
        self._leases[service_addr] = lease
        return lease

    def _form_service_key(self, service_name, service_addr):
        """Return service's key in etcd."""
        return '/'.join((service_name, service_addr))

    def register(self, service_name, service_addr, service_ttl, addr_cls=None, metadata=None):
        """Register gRPC services with the same address.

        :param service_name: A collection of gRPC service name.
        :param service_addr: gRPC server address.
        :param service_ttl: gRPC service ttl(seconds).
        :param addr_cls: format class of gRPC service address.
        :param metadata: extra meta data for JsonAddress.

        """
        lease = self.get_lease(service_addr, service_ttl)
        addr_cls = addr_cls or PlainAddress
        for service_name in service_name:
            key = self._form_service_key(service_name, service_addr)
            if addr_cls == JsonAddress:
                addr_obj = addr_cls(service_addr, metadata=metadata)
            else:
                addr_obj = addr_cls(service_addr)

            addr_val = addr_obj.add_value()
            self._client.put(key, addr_val, lease=lease)
            try:
                self._services[service_addr].add(service_name)
            except KeyError:
                self._services[service_addr] = {service_name}

    def heartbeat(self, service_addr=None):
        """gRPC service heartbeat."""
        if service_addr:
            lease = self.get_lease(service_addr)
            leases = ((service_addr, lease),)
        else:
            leases = tuple(self._leases.items())

        for service_addr, lease in leases:
            ret = lease.refresh()[0]
            if ret.TTL == 0:
                self.register(self._services[service_addr], service_addr, lease.ttl)

    def unregister(self, service_name, service_addr, addr_cls=None):
        """Unregister gRPC services with the same address.

        :param service_name: A collection of gRPC service name.
        :param service_addr: gRPC server address.

        """
        addr_cls = addr_cls or PlainAddress
        etcd_delete = True
        if addr_cls != PlainAddress:
            etcd_delete = False

        for service_name in service_name:
            key = self._form_service_key(service_name, service_addr)
            if etcd_delete:
                self._client.delete(key)
            else:
                self._client.put(addr_cls(service_addr).delete_value())

            self._services.get(service_addr, {}).discard(service_name)


class ZkServiceRegistry(ServiceRegistry):
    def __init__(self, zkServers, register_group, session_timeout=30):
        """

        :param zkServers:
        :param register_group: sample 'grpc-micro-service-group'
        :param session_timeout:
        """
        retry_policy = KazooRetry(max_tries=-1)
        self._client = KazooClient(hosts=zkServers,
                                   timeout=session_timeout,
                                   connection_retry=retry_policy,  # 重试策略
                                   logger=logger)

        self._register_group = register_group

    def register(self, service_name, service_addr, service_ttl):
        self._client.start()
        node_path = os.path.join(self._register_group, service_name, 'providers')
        # client.ensure_path(node_path)
        retry = KazooRetry(max_tries=3, ignore_expire=False)
        retry(self._client.ensure_path, node_path)

        node_kv = (service_name, service_addr)
        # client.create(os.path.join(node_path, node_kv[0]), node_kv[1])
        retry(self._client.create, os.path.join(node_path, node_kv[0]), str.encode(node_kv[1]))
        self._client.stop()

    def heartbeat(self, service_addr=None):
        pass

    def unregister(self, service_name, service_addr):
        self._client.start()
        node_path = os.path.join(self._register_group, service_name, 'providers')
        node = os.path.join(node_path, service_name)
        self._client.delete(node)
        self._client.stop()


class EurekaServiceRegistry(ServiceRegistry):
    def __init__(self, eureka_servers, session_timeout=30):
        """

        :param eureka_servers:
        :param session_timeout:
        """
        self._eureka_servers = eureka_servers
        self._session_timeout = session_timeout

    def register(self, service_name, service_addr, service_ttl, duration=30, service_port=0, management_port=8080, metadata={}):
        """

        :param service_name: sample 'grpc-analysis-service-provider'
        :param service_addr:
        :param service_ttl: 心跳间隔
        :param duration: 心跳超时
        :param service_port:
        :param metadata:
        :return:
        """
        instance_id = f'{service_name}:{service_addr}:{service_port}'
        metadata['gRPC.port'] = service_port  # 兼容springcloud调用
        eureka_client.init_registry_client(eureka_server=self._eureka_servers,
                                           app_name=service_name,
                                           instance_id=instance_id,
                                           instance_ip=service_addr,
                                           instance_port=management_port,
                                           status_page_url='actuator/info',  # 兼容springcloud访问
                                           health_check_url='actuator/health',
                                           renewal_interval_in_secs=service_ttl,
                                           duration_in_secs=duration,
                                           metadata=metadata
                                           )

    def heartbeat(self, service_addr=None):
        """gRPC service heartbeat."""
        pass

    def unregister(self, service_name, service_addr):
        pass
