#!/usr/bin/env python
# coding: utf-8

"""
@Author     :zhuzhibin@qdtech.ai
@Date       :2020/11/24
@Desc       :
"""
from opncom.logger import logger

from grpccloud.rpc.proto import helloword_pb2_grpc, helloword_pb2


class MessageServiceRpcService(helloword_pb2_grpc.MessageServiceServicer):
    def __init__(self):
        pass

    def message(self, request, context):
        logger.info(f'receive {request}')
        message = request.message
        response = helloword_pb2.ReceiveMessage(message=message)
        return response
