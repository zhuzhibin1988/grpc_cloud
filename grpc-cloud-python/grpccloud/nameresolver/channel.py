"""gRPC load balance channel module."""

import random

import grpc
from grpc import _common, _compression
from grpc._channel import (
    _UNARY_UNARY_INITIAL_DUE, _UNARY_STREAM_INITIAL_DUE,
    _STREAM_UNARY_INITIAL_DUE, _STREAM_STREAM_INITIAL_DUE,
    _EMPTY_FLAGS, _InitialMetadataFlags, _determine_deadline, _MultiThreadedRendezvous,
    _stream_unary_invocation_operationses_and_tags, _stream_unary_invocation_operationses)
from grpc._channel import (
    _RPCState, _ChannelCallState, _ChannelConnectivityState)
from grpc._channel import (
    _start_unary_request, _end_unary_response_blocking,
    _consume_request_iterator, _channel_managed_call_management,
    _handle_event, _event_handler, _deadline)
from grpc._cython import cygrpc

__all__ = ['RandomChannel', 'RoundrobinChannel']


class _UnaryUnaryMultiCallable(grpc.UnaryUnaryMultiCallable):

    def __init__(self,
                 channel,
                 method,
                 request_serializer,
                 response_deserializer):
        self._channel = channel
        self._method = method
        self._request_serializer = request_serializer
        self._response_deserializer = response_deserializer
        self._context = cygrpc.build_census_context()

    def _prepare(self,
                 request,
                 timeout,
                 metadata,
                 wait_for_ready,
                 compression):
        deadline, serialized_request, rendezvous = _start_unary_request(request, timeout, self._request_serializer)
        initial_metadata_flags = _InitialMetadataFlags().with_wait_for_ready(
            wait_for_ready)
        augmented_metadata = _compression.augment_metadata(
            metadata, compression)
        if serialized_request is None:
            return None, None, None, rendezvous
        else:
            state = _RPCState(_UNARY_UNARY_INITIAL_DUE, None, None, None, None)
            operations = (
                cygrpc.SendInitialMetadataOperation(augmented_metadata, initial_metadata_flags),
                cygrpc.SendMessageOperation(serialized_request, _EMPTY_FLAGS),
                cygrpc.SendCloseFromClientOperation(_EMPTY_FLAGS),
                cygrpc.ReceiveInitialMetadataOperation(_EMPTY_FLAGS),
                cygrpc.ReceiveMessageOperation(_EMPTY_FLAGS),
                cygrpc.ReceiveStatusOnClientOperation(_EMPTY_FLAGS),
            )
            return state, operations, deadline, None

    def _blocking(self,
                  request,
                  timeout,
                  metadata,
                  credentials,
                  wait_for_ready,
                  compression):
        state, operations, deadline, rendezvous = self._prepare(request, timeout, metadata, wait_for_ready, compression)
        if state is None:
            raise rendezvous  # pylint: disable-msg=raising-bad-type
        else:
            call = self._channel.segregated_call(cygrpc.PropagationConstants.GRPC_PROPAGATE_DEFAULTS,
                                                 self._method,
                                                 None,
                                                 _determine_deadline(deadline),
                                                 metadata,
                                                 None if credentials is None else credentials._credentials,
                                                 ((operations, None,),),
                                                 self._context)
            event = call.next_event()
            _handle_event(event, state, self._response_deserializer)
            return state, call

    def __call__(self,
                 request,
                 timeout=None,
                 metadata=None,
                 credentials=None,
                 wait_for_ready=None,
                 compression=None):
        state, call = self._blocking(request, timeout, metadata, credentials, wait_for_ready, compression)
        return _end_unary_response_blocking(state, call, False, None)

    def with_call(self,
                  request,
                  timeout=None,
                  metadata=None,
                  credentials=None,
                  wait_for_ready=None,
                  compression=None):
        state, call = self._blocking(request, timeout, metadata, credentials, wait_for_ready, compression)
        return _end_unary_response_blocking(state, call, True, None)

    def future(self,
               request,
               timeout=None,
               metadata=None,
               credentials=None,
               wait_for_ready=None,
               compression=None):
        state, operations, deadline, rendezvous = self._prepare(request, timeout, metadata, wait_for_ready, compression)
        if state is None:
            raise rendezvous
        else:
            event_handler = _event_handler(state, self._response_deserializer)
            call = self._channel.managed_call(cygrpc.PropagationConstants.GRPC_PROPAGATE_DEFAULTS,
                                              self._method,
                                              None,
                                              deadline,
                                              metadata,
                                              None if credentials is None else credentials._credentials,
                                              (operations,),
                                              event_handler,
                                              self._context)
            return _MultiThreadedRendezvous(state, call, self._response_deserializer, deadline)


class _UnaryStreamMultiCallable(grpc.UnaryStreamMultiCallable):

    def __init__(self,
                 channel,
                 method,
                 request_serializer,
                 response_deserializer):
        self._channel = channel
        self._method = method
        self._request_serializer = request_serializer
        self._response_deserializer = response_deserializer
        self._context = cygrpc.build_census_context()

    def __call__(self,
                 request,
                 timeout=None,
                 metadata=None,
                 credentials=None,
                 wait_for_ready=None,
                 compression=None):
        deadline, serialized_request, rendezvous = _start_unary_request(request, timeout, self._request_serializer)
        initial_metadata_flags = _InitialMetadataFlags().with_wait_for_ready(wait_for_ready)
        if serialized_request is None:
            raise rendezvous
        else:
            augmented_metadata = _compression.augment_metadata(metadata, compression)
            state = _RPCState(_UNARY_STREAM_INITIAL_DUE, None, None, None, None)
            operationses = (
                (
                    cygrpc.SendInitialMetadataOperation(augmented_metadata, initial_metadata_flags),
                    cygrpc.SendMessageOperation(serialized_request, _EMPTY_FLAGS),
                    cygrpc.SendCloseFromClientOperation(_EMPTY_FLAGS),
                    cygrpc.ReceiveStatusOnClientOperation(_EMPTY_FLAGS),
                ),
                (cygrpc.ReceiveInitialMetadataOperation(_EMPTY_FLAGS),),
            )

            call = self._channel.managed_call(cygrpc.PropagationConstants.GRPC_PROPAGATE_DEFAULTS,
                                              self._method,
                                              None,
                                              _determine_deadline(deadline),
                                              metadata,
                                              None if credentials is None else credentials._credentials,
                                              operationses,
                                              _event_handler(state, self._response_deserializer),
                                              self._context)
            return _MultiThreadedRendezvous(state, call, self._response_deserializer, deadline)


class _StreamUnaryMultiCallable(grpc.StreamUnaryMultiCallable):

    def __init__(self,
                 channel,
                 method,
                 request_serializer,
                 response_deserializer):
        self._channel = channel
        self._method = method
        self._request_serializer = request_serializer
        self._response_deserializer = response_deserializer
        self._context = cygrpc.build_census_context()

    def _blocking(self,
                  request_iterator,
                  timeout,
                  metadata,
                  credentials,
                  wait_for_ready,
                  compression):
        deadline = _deadline(timeout)
        state = _RPCState(_STREAM_UNARY_INITIAL_DUE, None, None, None, None)
        initial_metadata_flags = _InitialMetadataFlags().with_wait_for_ready(wait_for_ready)
        augmented_metadata = _compression.augment_metadata(metadata, compression)

        call = self._channel.segregated_call(cygrpc.PropagationConstants.GRPC_PROPAGATE_DEFAULTS,
                                             self._method,
                                             None,
                                             _determine_deadline(deadline),
                                             augmented_metadata,
                                             None if credentials is None else credentials._credentials,
                                             _stream_unary_invocation_operationses_and_tags(augmented_metadata, initial_metadata_flags),
                                             self._context)
        _consume_request_iterator(request_iterator, state, call, self._request_serializer, None)
        while True:
            event = call.next_event()
            with state.condition:
                _handle_event(event, state, self._response_deserializer)
                state.condition.notify_all()
                if not state.due:
                    break
        return state, call

    def __call__(self,
                 request_iterator,
                 timeout=None,
                 metadata=None,
                 credentials=None,
                 wait_for_ready=None,
                 compression=None):
        state, call, = self._blocking(request_iterator, timeout, metadata, credentials, wait_for_ready, compression)
        return _end_unary_response_blocking(state, call, False, None)

    def with_call(self,
                  request_iterator,
                  timeout=None,
                  metadata=None,
                  credentials=None,
                  wait_for_ready=None,
                  compression=None):
        state, call, = self._blocking(request_iterator, timeout, metadata, credentials, wait_for_ready, compression)
        return _end_unary_response_blocking(state, call, True, None)

    def future(self,
               request_iterator,
               timeout=None,
               metadata=None,
               credentials=None,
               wait_for_ready=None,
               compression=None):
        deadline = _deadline(timeout)
        state = _RPCState(_STREAM_UNARY_INITIAL_DUE, None, None, None, None)
        event_handler = _event_handler(state, self._response_deserializer)
        initial_metadata_flags = _InitialMetadataFlags().with_wait_for_ready(
            wait_for_ready)
        augmented_metadata = _compression.augment_metadata(
            metadata, compression)
        call = self._channel.managed_call(cygrpc.PropagationConstants.GRPC_PROPAGATE_DEFAULTS,
                                          self._method,
                                          None,
                                          deadline,
                                          augmented_metadata,
                                          None if credentials is None else credentials._credentials,
                                          _stream_unary_invocation_operationses(metadata, initial_metadata_flags),
                                          event_handler,
                                          self._context)
        _consume_request_iterator(request_iterator, state, call, self._request_serializer, event_handler)
        return _MultiThreadedRendezvous(state, call, self._response_deserializer, deadline)


class _StreamStreamMultiCallable(grpc.StreamStreamMultiCallable):

    def __init__(self,
                 channel,
                 method,
                 request_serializer,
                 response_deserializer):
        self._channel = channel
        self._method = method
        self._request_serializer = request_serializer
        self._response_deserializer = response_deserializer
        self._context = cygrpc.build_census_context()

    def __call__(self,
                 request_iterator,
                 timeout=None,
                 metadata=None,
                 credentials=None,
                 wait_for_ready=None,
                 compression=None):
        deadline = _deadline(timeout)
        state = _RPCState(_STREAM_STREAM_INITIAL_DUE, None, None, None, None)
        initial_metadata_flags = _InitialMetadataFlags().with_wait_for_ready(
            wait_for_ready)
        augmented_metadata = _compression.augment_metadata(
            metadata, compression)
        operationses = (
            (
                cygrpc.SendInitialMetadataOperation(augmented_metadata, initial_metadata_flags),
                cygrpc.ReceiveStatusOnClientOperation(_EMPTY_FLAGS),
            ),
            (cygrpc.ReceiveInitialMetadataOperation(_EMPTY_FLAGS),),
        )
        event_handler = _event_handler(state, self._response_deserializer)
        call = self._channel.managed_call(cygrpc.PropagationConstants.GRPC_PROPAGATE_DEFAULTS,
                                          self._method,
                                          None,
                                          _determine_deadline(deadline),
                                          augmented_metadata,
                                          None if credentials is None else credentials._credentials,
                                          operationses,
                                          event_handler,
                                          self._context)
        _consume_request_iterator(request_iterator, state, call, self._request_serializer, event_handler)
        return _MultiThreadedRendezvous(state, call, self._response_deserializer, deadline)


class Channel(object):
    """An object communicates between `LbChannel` and gRPC request."""

    __slots__ = ('target', 'channel', 'managed_call', 'connectivity_state')

    def __init__(self, target, options=None, credentials=None):
        options = options if options is not None else ()
        self.target = target
        self.channel = channel = cygrpc.Channel(_common.encode(target),
                                                options,
                                                credentials)
        self.managed_call = _channel_managed_call_management(_ChannelCallState(channel))
        self.connectivity_state = _ChannelConnectivityState(channel)


class LbChannel(grpc.Channel):
    """A gRPC load balance channel."""

    def __init__(self, service_name, resolver):
        self._service_name = service_name
        self._resolver = resolver
        self._channels = {}

    def select_target(self):
        raise NotImplementedError

    def get_channel(self):
        addr = self.select_target()
        try:
            return self._channels[addr]
        except KeyError:
            channel = Channel(addr)
            self._channels[addr] = channel
            return channel

    def release_channel(self, channel):
        name = self._service_name
        items = {name: ((), (channel.target,))}
        self._resolver.update(**items)

    def subscribe(self, callback, try_to_connect=False):
        raise NotImplementedError

    def unsubscribe(self, callback):
        raise NotImplementedError

    def unary_unary(self, method, request_serializer=None, response_deserializer=None):
        return _UnaryUnaryMultiCallable(self.get_channel().channel,
                                        _common.encode(method),
                                        request_serializer,
                                        response_deserializer)

    def unary_stream(self, method, request_serializer=None, response_deserializer=None):
        return _UnaryStreamMultiCallable(self.get_channel().channel,
                                         _common.encode(method),
                                         request_serializer,
                                         response_deserializer)

    def stream_unary(self, method, request_serializer=None, response_deserializer=None):
        return _StreamUnaryMultiCallable(self.get_channel().channel,
                                         _common.encode(method),
                                         request_serializer,
                                         response_deserializer)

    def stream_stream(self, method, request_serializer=None, response_deserializer=None):
        return _StreamStreamMultiCallable(self.get_channel().channel,
                                          _common.encode(method),
                                          request_serializer,
                                          response_deserializer)

    def __del__(self):
        del self._resolver

    def close(self):
        raise NotImplementedError


class RandomChannel(LbChannel):
    """Random gRPC load balance channel."""

    def select_target(self):
        addrs = self._resolver.resolve(self._service_name)
        addr_idx = random.randint(0, len(addrs) - 1)
        addr = addrs[addr_idx]
        return addr


class RoundrobinChannel(LbChannel):
    """Roundrobin gRPC load balance channel."""

    def __init__(self, service_name, resolver):
        super(RoundrobinChannel, self).__init__(service_name, resolver)
        self._cur_index = 0

    def select_target(self):
        addrs = self._resolver.resolve(self._service_name)
        addr_num = len(addrs)
        if addr_num == 0:
            raise ValueError('No channel.')

        addr = addrs[self._cur_index % addr_num]
        self._cur_index = (self._cur_index + 1) % addr_num

        return addr
