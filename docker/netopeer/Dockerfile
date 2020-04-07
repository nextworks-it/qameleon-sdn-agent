##Into docker
FROM centos:7

# install required packages
RUN ["yum", "install", "-y", "epel-release"]
RUN ["yum", "install", "-y", "git", "make", "libtool", "libxml2-devel", "file", "libxslt-devel", "libssh-devel", "libcurl-devel", "python3", "python-pip", "libxml2-python", "openssh-server", "augeas-devel","nano"]
RUN ["ssh-keygen", "-A"]
RUN ["pip", "install", "pyang"]

# clone, build and install libnetconf
RUN set -e -x; \
    git clone https://github.com/CESNET/libnetconf.git /usr/src/libnetconf; \
    cd /usr/src/libnetconf; \
autoreconf --force --install; \
    ./configure --prefix='/usr';\
    make; \
    make install; \
    ln -s /usr/lib/pkgconfig/libnetconf.pc /usr/lib64/pkgconfig/

# build and install netopeer-server
COPY server /usr/src/netopeer/server
RUN set -e -x; \
    cd /usr/src/netopeer/server; \
    ./configure; \
    make; \
    make install; \
    mkdir /usr/etc/netopeer; \
    mkdir /usr/etc/netopeer/cfgnetopeer; \
    cp -v config/datastore.xml /usr/etc/netopeer/cfgnetopeer/datastore.xml

# build and install netopeer-cli
COPY cli /usr/src/netopeer/cli
RUN set -e -x; \
     yum install -y readline-devel.x86_64; \
    cd /usr/src/netopeer/cli; \
    ls -lah; \
    ./configure; \
    make; \
    make install;



# build and install transAPI/cfgsystem
COPY transAPI/cfgsystem /usr/src/netopeer/cfgsystem
RUN set -e -x; \
    cd /usr/src/netopeer/cfgsystem; \
    ./configure; \
    make; \
    make install; \
    mkdir /usr/etc/netopeer/ietf-system; \
    touch /usr/etc/netopeer/ietf-system/datastore.xml; \
    sed -i '/<hostname>/d' /usr/etc/netopeer/ietf-system/datastore.xml; \
    cp -r /usr/local/lib/python2.7/site-packages/netopeer /usr/lib/python2.7/site-packages/netopeer;



# build and install netopeer_libs through netopeer-manager
COPY netopeer_libs /usr/src/netopeer/netopeer_libs

#Create dir for datastore
RUN mkdir /home/datastore

RUN touch /usr/local/etc/netopeer/cfgnetopeer/datastore.xml

# build and install tpa
RUN set -e -x; \
    cd /usr/src/netopeer/netopeer_libs/tpa; \
    lnctool --model tpa.yang convert; \
    lnctool --model tpa.yang validation; \
    lnctool --model tpa.yang transapi --paths ./pathfile; \
    ./configure; \
    make install; \
    netopeer-manager add --name tpa --model /usr/src/netopeer/netopeer_libs/tpa/tpa.yin --datastore /home/datastore/datastore-tpa.xml --transapi /usr/local/lib/tpa.so;



# build and install wss
RUN set -e -x; \
    cd /usr/src/netopeer/netopeer_libs/wss; \
    lnctool --model wss.yang convert; \
    lnctool --model wss.yang validation; \
    lnctool --model wss.yang transapi --paths ./pathfile; \
    ./configure; \
    make install; \
    netopeer-manager add --name wss --model /usr/src/netopeer/netopeer_libs/wss/wss.yin --datastore /home/datastore/datastore-wss.xml --transapi /usr/local/lib/wss.so;


#Setting up the startup config reader and the socket server
COPY startup_config_diff /usr/src/netopeer/startup_config_diff
RUN mkdir /home/cfg_files/
RUN touch /home/cfg_files/conf-tpa.txt
RUN touch /home/cfg_files/conf-wss.txt

##Installing lib for socket server script
RUN pip3 install xmltodict
COPY socket_server /usr/src/netopeer/socket_server

RUN rm /usr/src/netopeer/startup_config_diff/config_docker.json
COPY wrapper.sh /usr/src/netopeer/wrapper.sh

CMD ["netopeer-server", "-v", "7"]


# expose ports
EXPOSE 830
