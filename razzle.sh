#!/bin/bash

#输入参数
razzle_phase=build
razzle_environment=dev

if [ "$1" != "" ]
then
razzle_phase=$1
fi
echo razzle_phase=$razzle_phase

if [ "$2" != "" ]
then
razzle_environment=$2
fi
echo razzle_environment=$razzle_environment


#编译打包部署docker化的spring cloud程序
#作者：紫树

#当前路径
current_path=$(pwd)
echo current_path=$current_path

#应用名称
application_name=$(mvn -q \
    -Dexec.executable=echo \
    -Dexec.args='${project.artifactId}' \
    --non-recursive \
    exec:exec)
application_version=$(mvn -q \
    -Dexec.executable=echo \
    -Dexec.args='${project.version}' \
    --non-recursive \
    exec:exec)

#echo application_name=$application_name
#echo application_version=$application_version

#git相关
git_branch=$(git branch | sed -n -e 's/^\* \(.*\)/\1/p')

#docker镜像库地址和tag
docker_registry_dev_host=dockerregistry.qianxiantech.com
docker_registry_test_host=192.168.88.13/microservicesdemo
docker_registry_online_host=uhub.service.ucloud.cn/qx_douqian
docker_registry_path=/microservicesdemo
if [ "$razzle_environment" == "dev" ]
then
docker_registry=${docker_registry_dev_host}${docker_registry_path}
elif [ "$razzle_environment" == "test" ]
then
docker_registry=$docker_registry_test_host
elif [ "$razzle_environment" == "online" ]
then
docker logout uhub.service.ucloud.cn
docker login -u wangguolei@qianxiantech.com -p "2018*Qianxian&~" uhub.service.ucloud.cn
docker_registry=$docker_registry_online_host
fi
echo docker_registry = $docker_registry

docker_image_tag=$git_branch-`date '+%Y%m%d%H%M%S'`
docker_image=${application_name}_$application_version:$docker_image_tag
docker_image=$(echo $docker_image | tr "[:upper:]" "[:lower:]")
#echo docker_image=$docker_image

#docker file相对位置
docker_file_location=/src/main/docker/Dockerfile

#maven 编译
mvn clean package -Dmaven.test.skip

#docker 打包
cd $current_path/target
docker build -t $docker_image -f $current_path/$docker_file_location --network=host .
cd $current_path

if [ "$razzle_phase" == "deploy" ]
then
#如果是deploy,上传docker镜像库
docker tag $docker_image $docker_registry/$docker_image
docker push $docker_registry/$docker_image
fi
