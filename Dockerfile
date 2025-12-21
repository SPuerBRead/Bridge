FROM centos:7
RUN cd /etc/yum.repos.d/
RUN sed -i 's/mirrorlist/#mirrorlist/g' /etc/yum.repos.d/CentOS-*
RUN sed -i 's|#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g' 	/etc/yum.repos.d/CentOS-*
RUN yum makecache
RUN yum update -y
RUN yum install java-1.8.0-openjdk* -y

RUN yum install wget -y

RUN yum install epel-release -y
RUN yum install maven -y

RUN mkdir /bridge
COPY . /bridge

WORKDIR /bridge

RUN yum install which -y

RUN export JAVA_HOME=$(dirname $(dirname $(readlink $(readlink $(which javac)))))

RUN mvn clean package -DskipTest
