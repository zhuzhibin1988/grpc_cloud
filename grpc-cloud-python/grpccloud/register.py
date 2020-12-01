#!/usr/bin/env python
# coding: utf-8

"""
@Author     :zhuzhibin@qdtech.ai
@Date       :2020/11/13
@Desc       :
"""
import os

from kazoo.client import KazooClient
from kazoo.retry import KazooRetry
from opncom.logger import logger

retry_policy = KazooRetry(max_tries=-1)
client = KazooClient(hosts='127.0.0.1:2181',
                     timeout=10.0,  # 会话超时
                     connection_retry=retry_policy,  # 重试策略
                     logger=logger)

register_group = 'grpc-micro-service-group'


class ServiceRegister(object):

    @staticmethod
    def register(unique, schema, url, name):
        client.start()
        node_path = os.path.join(register_group, name, 'providers')
        # client.ensure_path(node_path)
        retry = KazooRetry(max_tries=3, ignore_expire=False)
        retry(client.ensure_path, node_path)

        node_kv = (name + ':' + unique, schema + ':' + url)
        # client.create(os.path.join(node_path, node_kv[0]), node_kv[1])
        retry(client.create, os.path.join(node_path, node_kv[0]), str.encode(node_kv[1]))
        client.stop()

    @staticmethod
    def unregister(unique, schema, url, name):
        client.start()
        node_path = os.path.join(register_group, name, 'providers')
        node = os.path.join(node_path, name + ':' + unique)
        client.delete(node)
        client.stop()
