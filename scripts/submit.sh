#!/bin/bash
username='user1'
analysis_name='analysis1'
jarpy='jar'
while getopts ":u:n:t:p:" o; do
    case "${o}" in
        u)
            username=${OPTARG}
            ;;
        n)
            analysis_name=${OPTARG}
            ;;
        t)
            jarpy=${OPTARG}
            ;;
         p)
            params=${OPTARG}
            ;;
        *)
            ;;
    esac
done
shift $((OPTIND-1))

classifier=$1
mkdir -p /bd2k/$username/$analysis_name/
cp $classifier /bd2k/$username/$analysis_name/$classifier

if [ $jarpy = jar ]; then
	docker run --rm -v /bd2k/$username/$analysis_name:/data -w /data java:7 java -jar $classifier $params
else
	docker run -it --rm --name my-running-script -v /bd2k/$username/$analysis_name:/data -w /data python:3 python $classifier $params
fi

touch /bd2k/$username/$analysis_name/'done'