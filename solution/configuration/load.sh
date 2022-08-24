#!/bin/bash

CONFIG_FILE=app.config
COMMENT='#'
INIT_FILENAME=init
HOLDER=''

HOLDER="${HOLDER}\ndeleteall /platform"
HOLDER="${HOLDER}\ndeleteall /services"

while read line; do

    if [[ $line != $COMMENT** ]] && [ -n "$line" ]; then

        entry=(${line//=/ })

        if [ -n "${entry[1]}" ]; then

            HOLDER="${HOLDER}\ncreate ${entry[0]} ${entry[1]}"

        else

            HOLDER="${HOLDER}\ncreate ${entry[0]}"

        fi

    fi

done < $CONFIG_FILE

echo -e $HOLDER
