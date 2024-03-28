aws logs describe-log-streams --log-group-name '/aws/lambda/klaimz-dev-core' --output text | awk '{print $7}' | while read x;
do aws logs delete-log-stream --log-group-name '/aws/lambda/klaimz-dev-core' --log-stream-name $x
done