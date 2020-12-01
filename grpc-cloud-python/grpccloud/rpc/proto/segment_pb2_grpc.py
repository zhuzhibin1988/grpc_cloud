# Generated by the gRPC Python protocol compiler plugin. DO NOT EDIT!
"""Client and server classes corresponding to protobuf-defined services."""
import grpc

import grpccloud.rpc.proto.segment_pb2 as segment__pb2


class TextSegmentServiceStub(object):
    """定义service 
    """

    def __init__(self, channel):
        """Constructor.

        Args:
            channel: A grpc.Channel.
        """
        self.addExternalWord = channel.unary_unary(
                '/TextSegmentService/addExternalWord',
                request_serializer=segment__pb2.DictionaryRequest.SerializeToString,
                response_deserializer=segment__pb2.DictionaryResponse.FromString,
                )
        self.removeExternalWord = channel.unary_unary(
                '/TextSegmentService/removeExternalWord',
                request_serializer=segment__pb2.DictionaryRequest.SerializeToString,
                response_deserializer=segment__pb2.DictionaryResponse.FromString,
                )
        self.segment = channel.unary_unary(
                '/TextSegmentService/segment',
                request_serializer=segment__pb2.SegmentRequest.SerializeToString,
                response_deserializer=segment__pb2.SegmentResponse.FromString,
                )
        self.heartbeat = channel.unary_unary(
                '/TextSegmentService/heartbeat',
                request_serializer=segment__pb2.HeartbeatRequest.SerializeToString,
                response_deserializer=segment__pb2.HeartbeatResponse.FromString,
                )


class TextSegmentServiceServicer(object):
    """定义service 
    """

    def addExternalWord(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def removeExternalWord(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def segment(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')

    def heartbeat(self, request, context):
        """Missing associated documentation comment in .proto file."""
        context.set_code(grpc.StatusCode.UNIMPLEMENTED)
        context.set_details('Method not implemented!')
        raise NotImplementedError('Method not implemented!')


def add_TextSegmentServiceServicer_to_server(servicer, server):
    rpc_method_handlers = {
            'addExternalWord': grpc.unary_unary_rpc_method_handler(
                    servicer.addExternalWord,
                    request_deserializer=segment__pb2.DictionaryRequest.FromString,
                    response_serializer=segment__pb2.DictionaryResponse.SerializeToString,
            ),
            'removeExternalWord': grpc.unary_unary_rpc_method_handler(
                    servicer.removeExternalWord,
                    request_deserializer=segment__pb2.DictionaryRequest.FromString,
                    response_serializer=segment__pb2.DictionaryResponse.SerializeToString,
            ),
            'segment': grpc.unary_unary_rpc_method_handler(
                    servicer.segment,
                    request_deserializer=segment__pb2.SegmentRequest.FromString,
                    response_serializer=segment__pb2.SegmentResponse.SerializeToString,
            ),
            'heartbeat': grpc.unary_unary_rpc_method_handler(
                    servicer.heartbeat,
                    request_deserializer=segment__pb2.HeartbeatRequest.FromString,
                    response_serializer=segment__pb2.HeartbeatResponse.SerializeToString,
            ),
    }
    generic_handler = grpc.method_handlers_generic_handler(
            'TextSegmentService', rpc_method_handlers)
    server.add_generic_rpc_handlers((generic_handler,))


 # This class is part of an EXPERIMENTAL API.
class TextSegmentService(object):
    """定义service 
    """

    @staticmethod
    def addExternalWord(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/TextSegmentService/addExternalWord',
            segment__pb2.DictionaryRequest.SerializeToString,
            segment__pb2.DictionaryResponse.FromString,
            options, channel_credentials,
            call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def removeExternalWord(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/TextSegmentService/removeExternalWord',
            segment__pb2.DictionaryRequest.SerializeToString,
            segment__pb2.DictionaryResponse.FromString,
            options, channel_credentials,
            call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def segment(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/TextSegmentService/segment',
            segment__pb2.SegmentRequest.SerializeToString,
            segment__pb2.SegmentResponse.FromString,
            options, channel_credentials,
            call_credentials, compression, wait_for_ready, timeout, metadata)

    @staticmethod
    def heartbeat(request,
            target,
            options=(),
            channel_credentials=None,
            call_credentials=None,
            compression=None,
            wait_for_ready=None,
            timeout=None,
            metadata=None):
        return grpc.experimental.unary_unary(request, target, '/TextSegmentService/heartbeat',
            segment__pb2.HeartbeatRequest.SerializeToString,
            segment__pb2.HeartbeatResponse.FromString,
            options, channel_credentials,
            call_credentials, compression, wait_for_ready, timeout, metadata)
