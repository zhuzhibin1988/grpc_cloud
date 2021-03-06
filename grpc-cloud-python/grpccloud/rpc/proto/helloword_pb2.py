# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: helloword.proto

from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()




DESCRIPTOR = _descriptor.FileDescriptor(
  name='helloword.proto',
  package='',
  syntax='proto3',
  serialized_options=None,
  create_key=_descriptor._internal_create_key,
  serialized_pb=b'\n\x0fhelloword.proto\"\x1e\n\x0bSendMessage\x12\x0f\n\x07message\x18\x01 \x01(\t\"!\n\x0eReceiveMessage\x12\x0f\n\x07message\x18\x01 \x01(\t2:\n\x0eMessageService\x12(\n\x07message\x12\x0c.SendMessage\x1a\x0f.ReceiveMessageb\x06proto3'
)




_SENDMESSAGE = _descriptor.Descriptor(
  name='SendMessage',
  full_name='SendMessage',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='message', full_name='SendMessage.message', index=0,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=b"".decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=19,
  serialized_end=49,
)


_RECEIVEMESSAGE = _descriptor.Descriptor(
  name='ReceiveMessage',
  full_name='ReceiveMessage',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  create_key=_descriptor._internal_create_key,
  fields=[
    _descriptor.FieldDescriptor(
      name='message', full_name='ReceiveMessage.message', index=0,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=b"".decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      serialized_options=None, file=DESCRIPTOR,  create_key=_descriptor._internal_create_key),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  serialized_options=None,
  is_extendable=False,
  syntax='proto3',
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=51,
  serialized_end=84,
)

DESCRIPTOR.message_types_by_name['SendMessage'] = _SENDMESSAGE
DESCRIPTOR.message_types_by_name['ReceiveMessage'] = _RECEIVEMESSAGE
_sym_db.RegisterFileDescriptor(DESCRIPTOR)

SendMessage = _reflection.GeneratedProtocolMessageType('SendMessage', (_message.Message,), {
  'DESCRIPTOR' : _SENDMESSAGE,
  '__module__' : 'helloword_pb2'
  # @@protoc_insertion_point(class_scope:SendMessage)
  })
_sym_db.RegisterMessage(SendMessage)

ReceiveMessage = _reflection.GeneratedProtocolMessageType('ReceiveMessage', (_message.Message,), {
  'DESCRIPTOR' : _RECEIVEMESSAGE,
  '__module__' : 'helloword_pb2'
  # @@protoc_insertion_point(class_scope:ReceiveMessage)
  })
_sym_db.RegisterMessage(ReceiveMessage)



_MESSAGESERVICE = _descriptor.ServiceDescriptor(
  name='MessageService',
  full_name='MessageService',
  file=DESCRIPTOR,
  index=0,
  serialized_options=None,
  create_key=_descriptor._internal_create_key,
  serialized_start=86,
  serialized_end=144,
  methods=[
  _descriptor.MethodDescriptor(
    name='message',
    full_name='MessageService.message',
    index=0,
    containing_service=None,
    input_type=_SENDMESSAGE,
    output_type=_RECEIVEMESSAGE,
    serialized_options=None,
    create_key=_descriptor._internal_create_key,
  ),
])
_sym_db.RegisterServiceDescriptor(_MESSAGESERVICE)

DESCRIPTOR.services_by_name['MessageService'] = _MESSAGESERVICE

# @@protoc_insertion_point(module_scope)
