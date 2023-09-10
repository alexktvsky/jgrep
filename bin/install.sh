#!/usr/bin/env bash

BIN_DIR=$(dirname "$(realpath "$0")")
HOME_DIR=$(dirname "${BIN_DIR}")

if [ -z "$1" ]; then
    echo "No install directory passed"
    exit 1
fi

INSTALL_DIR=$1

mkdir "${INSTALL_DIR}/jgrep"
mkdir "${INSTALL_DIR}/jgrep/bin"
mkdir "${INSTALL_DIR}/jgrep/lib"
cp "${BIN_DIR}/jgrep" "${INSTALL_DIR}/jgrep/bin"
cp "${HOME_DIR}/target/jgrep.jar" "${INSTALL_DIR}/jgrep/lib"
