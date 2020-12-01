#!/usr/bin/env python
# coding: utf-8

"""
@Author     :zhuzhibin@qdtech.ai
@Date       :2020/11/25
@Desc       :
"""
import py_eureka_client.eureka_client as  eureka_client


def getAllApplicationInfo(client):
    for application in client.applications.applications:
        for instance in application.up_instances:
            print(instance.app, instance.ipAddr, instance.metadata['gRPC.port'])


def getApplicationInfo(client, app_name):
    for instance in client.applications.get_application(app_name).up_instances:
        print(instance.app, instance.ipAddr, instance.metadata['gRPC.port'])


service_name = 'helloworld-provider'.upper()
client = eureka_client.init(eureka_server='http://eureka.node1:10001/eureka/,http://eureka.node2:10002/eureka/,http://eureka.node3:10003/eureka/',
                            app_name='helloworld-consumer')
eureka_client.LeaseInfo
getAllApplicationInfo(client)
