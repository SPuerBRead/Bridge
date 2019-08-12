FROM centos:7

RUN yum install java-1.8.0-openjdk* -y

RUN yum install wget -y

RUN wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
RUN yum -y install apache-maven

RUN mkdir /bridge
COPY . /bridge

WORKDIR /bridge

RUN yum install which -y

RUN export JAVA_HOME=$(dirname $(dirname $(readlink $(readlink $(which javac)))))

RUN mvn clean package -DskipTes