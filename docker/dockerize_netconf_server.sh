#1. Clone netopeer remote repository
git clone https://github.com/CESNET/netopeer

#2. Replace Dockerfile into netopeer dir, with Dockerfile in this dir
cp Dockerfile netopeer/Dockerfile

#3. Copy the startup configuration checker into netopeer dir.
cp -r ../netconf_server_netopeer/startup_config_diff netopeer/
cp ../netconf_server_netopeer/startup_config_diff/config_docker.json netopeer/startup_config_diff/config.json

#4. Copy the netopeer libs developed (wss and tpa for now). The docker file will build and embed them into ithe netopeer serve
cp -r ../netconf_server_netopeer/netopeer_libs/ netopeer/
chmod 777 wrapper.sh
cp wrapper.sh netopeer/

#5. Copy the socket server that will receive and parse the xml request coming from the SDN controller.
mkdir netopeer/socket_server
cp ../netconf_server_netopeer/socket_server.py netopeer/socket_server

##Build docker file
cd netopeer
sudo docker build -t netopeer_server:0.1 .

##Show the just built image
sudo docker image ls

echo "Please run manually the container with sudo docker run <image_id>"

