#!/bin/sh

IS_MAC=$1
TIME_UNTIL_TIMEOUT=$2
PROCESS_ID=$3
SHOULD_RUN_PATH_ELSE_PRINT=$4

# check the process. Tto keep this as shell-agnostic as possible (function declaration syntax does vary from shell to shell),
# I'm just going to duplicate code.
if [ -z "$(ps -p$PROCESS_ID -opid= | grep $PROCESS_ID)" ]; then
    echo "Process was not running initially."
    exit 0
fi

# loop until the process with id PROCESS_ID no longer returns any result from PS, or TIME_UNTIL_TIMEOUT seconds have elapsed
TIME_WAITED=0
TIMED_OUT=0
while [ 1 ]; do
    if [ $TIME_WAITED -ge $TIME_UNTIL_TIMEOUT ]; then
        TIMED_OUT=1
        break;
    fi
    # the process terminated.
    if [ -z "$(ps -p$PROCESS_ID -opid= | grep $PROCESS_ID)" ]; then
        break;
    fi
	sleep 1
	TIME_WAITED=$(($TIME_WAITED+1))
done
# if the timeout period did elapse then give up.
if [ $TIMED_OUT -eq 0 ]; then
    # But if not, perform the behaviour to do upon process termination
    if [ $SHOULD_RUN_PATH_ELSE_PRINT = "true" ]; then
        # if the caller specified a path to run, then we'll run it.
        if [ $IS_MAC = "true" ]; then
            result=`pwd`
            while [[ "$result" != "/" ]] && [[ "$result" != *.app ]]
            do
                result="$(dirname "$result")"
            done
            if [[ "$result" != "/" ]]; then
                open "$result"
            else
                echo "Failed to find .app from `pwd`"
            fi
        else
            ./Geneious
        fi
    else
        # otherwise, print out a line of output saying that the process terminated.
        echo "Terminated.";
    fi
	# echo "about to send "$RESTART_COMMAND" to the shell" >> ~/RestartLog.txt
fi
