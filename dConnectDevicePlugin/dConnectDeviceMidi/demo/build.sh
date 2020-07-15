#!/bin/sh

NODE_MODULES=./node_modules
DIST=./dist

function build() {
    test -e ${NODE_MODULES}
    if [ $? -eq 1 ]
    then
        npm install
    fi

    test -e ${DIST}
    if [ $? -eq 1 ]
    then
        npm run build
    fi
}

function clean() {
    rm -fr ${DIST}
}

case "${1}" in
    "build" ) build ;;
    "clean" ) clean ;;
esac
