# Generated by the gRPC Python protocol compiler plugin. DO NOT EDIT!
"""Client and server classes corresponding to protobuf-defined services."""
import grpc

import grpccloud.rpc.proto.helloword_pb2 as helloword__pb2


class MessageServiceStub(object):
    """Missing associated documentation comment in .proto file."""

    def __init__(self, channel):
        """Constructor.

        Args:
            channel: A grpc.Channel.
        """
        self.message = channel.unary_unary(
            '/MessageService/message',
            request_serializer=helloword__pb2.SendMessage.SerializeToString,
            response_deserializer=helloword__pb2.ReceiveMessage.FromString,
        )


class MessageServiceServicer(object):
    """Missing associated documentation comment in .proto file."""

    def message(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')


def add_MessageServiceServicer_to_server(servicer, server):
    rpc_method_handlers = {
        'message': grpc.unary_unary_rpc_method_handler(
            servicer.message,
            request_deserializer=helloword__pb2.SendMessage.FromString,
            response_serializer=helloword__pb2.ReceiveMessage.SerializeToString,
        ),
    }
    generic_handler = grpc.method_handlers_generic_handler(
        'MessageService', rpc_method_handlers)
    server.add_generic_rpc_handlers((generic_handler,))


# This class is part of an EXPERIMENTAL API.
class MessageService(object):
    """Missing associated documentation comment in .proto file."""

    @staticmethod
    def message(request,
                target,
                options=(),
                channel_credentials=None,
                call_credentials=None,
                compression=None,
                wait_for_ready=None,
                timeout=None,
                metadata=None):
        return grpc.experimental.unary_unary(request, target, '/MessageService/message',
                                             helloword__pb2.SendMessage.SerializeToString,
                                             helloword__pb2.ReceiveMessage.FromString,
                                             options, channel_credentials,
                                             call_credentials, compression, wait_for_ready, timeout, metadata)
