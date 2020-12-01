#!/usr/bin/env python
# coding: utf-8

"""
@Author     :zhuzhibin@qdtech.ai
@Date       :2020/11/24
@Desc       :
"""
import time

from grpccloud.nameresolver.channel import RoundrobinChannel
from grpccloud.nameresolver.resolver import EurekaServiceResolver
from grpccloud.rpc.proto import helloword_pb2_grpc, helloword_pb2, segment_pb2_grpc, segment_pb2

nameresolver = EurekaServiceResolver(eureka_servers='http://eureka.node1:10001/eureka/,http://eureka.node2:10002/eureka/,http://eureka.node3:10003/eureka/')


def helloworld():
    channel = RoundrobinChannel('helloworld-provider', nameresolver)

    count = 0

    while True:
        try:
            count += 1
            stub = helloword_pb2_grpc.MessageServiceStub(channel)
            response = stub.message(helloword_pb2.SendMessage(message=f'{count} hello world'))
            print(response)
            time.sleep(0.5)
        except Exception as e:
            print(e)


def segment():
    channel = RoundrobinChannel('grpc-segment-service-provider', nameresolver)
    client = segment_pb2_grpc.TextSegmentServiceStub(channel)
    response = client.segment(segment_pb2.SegmentRequest(text=['我爱小红象'], wordNature=[], namespace=''))
    print(response)


helloworld()
