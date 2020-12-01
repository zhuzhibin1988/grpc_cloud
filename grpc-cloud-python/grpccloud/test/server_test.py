#!/usr/bin/env python
# coding: utf-8

"""
@Author     :zhuzhibin@qdtech.ai
@Date       :2020/11/13
@Desc       :
"""
import time
from concurrent import futures

import grpc
from opncom.logger import logger

from grpccloud.eureka.registry import EurekaServiceRegistry
from grpccloud.rpc.proto import helloword_pb2_grpc
from grpccloud.service.hellowordservice import MessageServiceRpcService

server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
# 将对应的任务处理函数添加到rpc server中
helloword_pb2_grpc.add_MessageServiceServicer_to_server(MessageServiceRpcService(), server)
# 这里使用的非安全接口，实际gRPC支持TLS/SSL安全连接，以及各种鉴权机制
host = '127.0.0.1'
port = 50004
management_port = 8088
server.add_insecure_port(f'{host}:{port}')
server.start()
logger.info(f'rpc service started, listening on {host}:{port}')

register = EurekaServiceRegistry(eureka_servers='http://eureka.node1:10001/eureka/,http://eureka.node2:10002/eureka/,http://eureka.node3:10003/eureka/')
register.register(service_name='helloworld-provider', service_addr=host, service_port=port, management_port=management_port, service_ttl=10)
logger.info(f'register application helloworld')

try:
    while True:
        time.sleep(60 * 60 * 24)
except KeyboardInterrupt:
    server.stop(0)
