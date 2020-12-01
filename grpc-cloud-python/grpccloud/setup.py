from setuptools import setup, find_packages
import os

version = '1.0.0'

setup(name='grpc-cloud-resolver',
      version=version,
      description=(
          "A simple Python gRPC service resolver and registry based on eureka. and compatible springcloud invoke"),
      long_description=open(
          os.path.join(os.path.dirname(__file__), 'README.rst')).read(),
      # classifiers=[
      #     'Development Status :: 3 - Alpha',
      #     'Intended Audience :: Developers',
      #     'Programming Language :: Python',
      #     'Programming Language :: Python :: 2',
      #     'Programming Language :: Python :: 2.7',
      #     'Programming Language :: Python :: 3',
      #     'Programming Language :: Python :: 3.4',
      #     'Programming Language :: Python :: 3.5',
      #     'Programming Language :: Python :: 3.6',
      #     'Topic :: Internet :: WWW/HTTP :: HTTP Servers',
      #     'Topic :: Software Development :: Version Control :: Git',
      #     'Topic :: System :: Networking',
      # ],
      keywords='gRPC eureka springcloud service resolver registry',
      author='zhuzhibin',
      author_email='zhuzhibin@qdtech.ai',
      url='https://github.com/zhuzhibin1988/grpc-cloud',
      license='Apache2.0',
      packages=find_packages(exclude=['ez_setup', 'examples', 'tests']),
      include_package_data=True,
      zip_safe=False,
      # setup_requires=('pytest-runner',),
      install_requires=(
          # -*- Extra requirements: -*-
          'grpcio>=1.30.0',
          'etcd3>=0.6.2',
          'py-eureka-client>=0.8.6'
      ),
      # tests_require=(
      #     'pytest',
      #     'pytest-runner',
      #     'pytest-mock>=1.6.0'
      # ),
      entry_points="""
      # -*- Entry points: -*-
      """
      )
