#!/usr/bin/env sh

# gets the topic id from a state file
get_topic_id() {
  JSON=`cat $OPERATOR_ID_1.json`
  JSON=$(echo "$JSON" | sed 's/^.*topicId":"//g')
  TOPIC_ID=$(echo "$JSON" | sed 's/"}//g')
  echo "Found TopicID $TOPIC_ID in $OPERATOR_ID_1.json"
}

echo "Recompile ?"
select yn in "Yes" "No"; do
    case $yn in
        Yes ) ./mvnw install -DskipTests; break;;
        No ) break;;
    esac
done

if [ -z $OPERATOR_SECRET_1 ]
then
  read -p "Enter FIRST operator PRIVATE key: " OPERATOR_SECRET_1
fi
if [ -z $OPERATOR_PUBLIC_1 ]
then
  read -p "Enter FIRST operator PUBLIC key: " OPERATOR_PUBLIC_1
fi
if [ -z $OPERATOR_ID_1 ]
then
  read -p "Enter FIRST operator account id: " OPERATOR_ID_1
fi

if [ -z $OPERATOR_SECRET_2 ]
then
  read -p "Enter SECOND operator PRIVATE key: " OPERATOR_SECRET_2
fi
if [ -z $OPERATOR_PUBLIC_2 ]
then
  read -p "Enter SECOND operator PRIVATE key: " OPERATOR_PUBLIC_2
fi
if [ -z $OPERATOR_ID_2 ]
then
  read -p "Enter SECOND operator account id: " OPERATOR_ID_2
fi

echo "Reset state ?"
select yn in "Yes" "No"; do
    case $yn in
        Yes ) rm *.json; break;;
        No ) break;;
    esac
done

# Operator 1
export OPERATOR_KEY=$OPERATOR_SECRET_1
export OPERATOR_ID=$OPERATOR_ID_1

# construct the token
java -jar hcs-token-example-1.0-run.jar construct TestToken TTT 2
# mint the token
java -jar hcs-token-example-1.0-run.jar mint 1000
# wait for a mirror update
java -jar hcs-token-example-1.0-run.jar refresh
# get the topic id from the json file for later
get_topic_id
# query information about the token
java -jar hcs-token-example-1.0-run.jar name
java -jar hcs-token-example-1.0-run.jar symbol
java -jar hcs-token-example-1.0-run.jar decimals
java -jar hcs-token-example-1.0-run.jar totalSupply
# query balance
java -jar hcs-token-example-1.0-run.jar balanceOf $OPERATOR_PUBLIC_1
# transfer 20 to another address
java -jar hcs-token-example-1.0-run.jar transfer $OPERATOR_PUBLIC_2 20
# wait for a mirror update
java -jar hcs-token-example-1.0-run.jar refresh
# query updated balances
java -jar hcs-token-example-1.0-run.jar balanceOf $OPERATOR_PUBLIC_1
java -jar hcs-token-example-1.0-run.jar balanceOf $OPERATOR_PUBLIC_2
# approve spender
java -jar hcs-token-example-1.0-run.jar approve $OPERATOR_PUBLIC_2 10
# wait for a mirror update
java -jar hcs-token-example-1.0-run.jar refresh
# query allowances
echo "should return 10"
java -jar hcs-token-example-1.0-run.jar allowance $OPERATOR_PUBLIC_1 $OPERATOR_PUBLIC_2
# increase allowance
java -jar hcs-token-example-1.0-run.jar increaseAllowance $OPERATOR_PUBLIC_2 20
# wait for a mirror update
java -jar hcs-token-example-1.0-run.jar refresh
# query allowances
echo "should return 30"
java -jar hcs-token-example-1.0-run.jar allowance $OPERATOR_PUBLIC_1 $OPERATOR_PUBLIC_2
# decrease allowance
java -jar hcs-token-example-1.0-run.jar decreaseAllowance $OPERATOR_PUBLIC_2 10
# wait for a mirror update
java -jar hcs-token-example-1.0-run.jar refresh
# query allowances
echo "should return 20"
java -jar hcs-token-example-1.0-run.jar allowance $OPERATOR_PUBLIC_1 $OPERATOR_PUBLIC_2
# burn
java -jar hcs-token-example-1.0-run.jar burn 20
# wait for a mirror update
java -jar hcs-token-example-1.0-run.jar refresh
# query updated balances
echo "should return 99960"
java -jar hcs-token-example-1.0-run.jar balanceOf $OPERATOR_PUBLIC_1
# query total supply
echo "should return 99980"
java -jar hcs-token-example-1.0-run.jar totalSupply

# switch operator
echo
echo "***** SWITCHING TO SECOND OPERATOR"
echo
export OPERATOR_KEY=$OPERATOR_SECRET_2
export OPERATOR_ID=$OPERATOR_ID_2

# join the network
java -jar hcs-token-example-1.0-run.jar join $TOPIC_ID

# wait for a mirror update to catch up history and local state
java -jar hcs-token-example-1.0-run.jar refresh
# transfer From
java -jar hcs-token-example-1.0-run.jar transferFrom $OPERATOR_PUBLIC_1 $OPERATOR_PUBLIC_2 10
# wait for a mirror update
java -jar hcs-token-example-1.0-run.jar refresh
# query updated balances
echo "should return 99950"
java -jar hcs-token-example-1.0-run.jar balanceOf $OPERATOR_PUBLIC_1
echo "should return 30"
java -jar hcs-token-example-1.0-run.jar balanceOf $OPERATOR_PUBLIC_2
echo "should return 10"
java -jar hcs-token-example-1.0-run.jar allowance $OPERATOR_PUBLIC_1 $OPERATOR_PUBLIC_2
