rm ../oj-jar/gateway/oj-gateway.jar
rm ../oj-jar/friend/oj-friend.jar
rm ../oj-jar/job/oj-job.jar
rm ../oj-jar/judge/oj-judge.jar
rm ../oj-jar/system/oj-system.jar
cp ../../../oj-gateway/target/oj-gateway-1.0.jar ../oj-jar/gateway/oj-gateway.jar
cp ../../../oj-modules/oj-judge/target/oj-judge-1.0-SNAPSHOT.jar ../oj-jar/judge/oj-judge.jar
cp ../../../oj-modules/oj-friend/target/oj-friend-1.0-SNAPSHOT.jar ../oj-jar/friend/oj-friend.jar
cp ../../../oj-modules/oj-job/target/oj-job-1.0-SNAPSHOT.jar ../oj-jar/job/oj-job.jar
cp ../../../oj-modules/oj-system/target/oj-system-1.0-SNAPSHOT.jar ../oj-jar/system/oj-system.jar