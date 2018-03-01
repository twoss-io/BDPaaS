#!/bin/bash
#
# worker nodes to distribute the image
#
#

worker_nodes=( bdp-wnode1 bdp-wnode2 bdp-wnode3 )
apex_sandbox_image=apexsandbox2:v0.1.2

helper()
{
    echo -e ""
    echo -e "Usage: $1 [0]"
    echo -e "\twith \"0\" option, docker image ${apex_sandbox_image} will be removed from worker nodes"
    echo -e "\twith \"1\" option, docker image ${apex_sandbox_image} will be built and distributed to every worker node"
    echo -e "\totherwise, ${apex_sandbox_image} will be built"
    echo -e ""
}


# --

([ "$1" = "-h" ] || [ "$1" = "--help" ]) && helper $0 && exit


#
# Remove image from master and workers
#
if [[ $1 = 0 ]]; then
	echo -e "\n>>> removing ${apex_sandbox_image} from master and workers...\n"
	docker rmi ${apex_sandbox_image}
	echo ""

	n=${#worker_nodes[@]}
	for ((i=0;i<$n;i++)); do
		echo -e ">>> remove image from worker: ${worker_nodes[$i]}"
		ssh ${worker_nodes[$i]} docker rmi ${apex_sandbox_image}
		echo -e ">>> checking image in ${worker_nodes[$i]}"
		ssh ${worker_nodes[$i]} docker images | grep sandbox2
		echo ""
	done
	exit 0
fi

#
# Build ubuntu 14.04 docker image
#
echo -e "\n>>> building ${apex_sandbox_image}...\n"

pushd ubuntu
docker build -t ${apex_sandbox_image} .
popd

echo ""

# check image
docker images | grep sandbox2 >/dev/null 2>&1
[[ $? != 0 ]] && echo -e "\n[ERR] apex container image does not exist!\n" && exit 1

if [[ $1 != 1 ]]; then
	# done & exit
	echo -e "\n-- done --\n"
	exit 0
fi

# distribute image to worker nodes
n=${#worker_nodes[@]}
for ((i=0;i<$n;i++)); do
	echo -e ">>> distribute image to worker: ${worker_nodes[$i]}"
	docker save ${apex_sandbox_image} | ssh ${worker_nodes[$i]} 'docker load'
	echo -e ">>> checking image in ${worker_nodes[$i]}"
	ssh ${worker_nodes[$i]} docker images | grep sandbox2
	echo ""
done

echo ""

exit 0
