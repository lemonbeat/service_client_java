#!/bin/bash
set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color


help() {
  printf "${GREEN} \r\n"
  printf "************************************************************************* \r\n"
  printf "To acquire a client certificate and use it with 'service_client_java', you need to \r\n"
  printf "1) Call the function 'create_csr' The generated CSR file can be sent to Lemonbeat in order to acquire a client certificate \r\n"
  printf "2) After receiving the client certificate .pem file you need to call the function 'convert_pem_to_service_client_java_compatible_jks_format', to generate the JKS file. \r\n"
  printf "3) Edit the file 'settings.properties' \r\n"
  printf "************************************************************************* \r\n"
  printf "Usage arguments: \r\n"
  printf "create_csr:                                               create CSR file, this file can be sent to Lemonbeat in order to acquire a client certificate.   \r\n"
  printf "convert_pem_to_service_client_java_compatible_jks_format: convert the certificate .pem file to JKS file, ready to be used with service_client_java. \r\n"
  printf "verify:                                                   after acquiring a client certificate you can use this function to verify the acquired certificate against the issuer CA. \r\n"
  printf "convert_pem_to_p12:                                       converts the certificate and the private_key into a p12 file.  \r\n"
  printf "convert_p12_to_jks:                                       converts a p12 file into jks file, saved in the alias client_cert. \r\n"
  printf "import_trust_store_into_jks:                              import trust store into an existing jks file \r\n\r\n"
  printf "${NC}"
}

create_csr() {
  printf "${GREEN} \r\n"
  printf "This command will generate two files a private key xxxxx.key.pem and a certificate signing request xxxxx.csr.pem  \r\n"
  printf "You can send the csr file to Lemonbeat in order to acquire a client certificate.  \r\n"
  printf "The infos you will enter, are meant only to identify and validate your request. The subject in the certificate you will receive may be different.   \r\n"
  printf "${NC} \r\n"

  [ -z $2 ] && read -e -p "Enter the stage of the installation (dev/test/com): " INSTALLATION_TYPE || INSTALLATION_TYPE=$2
  [ -z $3 ] && read -e -p "Enter the installation's name: " INSTALLATION_NAME || INSTALLATION_NAME=$3
  [ -z $4 ] && read -e -p "Enter the name of Organization Name: " PARTNER_NAME || PARTNER_NAME=$4
  [ -z $5 ] && read -e -p "Enter your Email: " EMAIL || EMAIL=$5

  openssl req -newkey rsa:4096 -keyout "${INSTALLATION_NAME}-${PARTNER_NAME}.key.pem" -out "${INSTALLATION_NAME}-${PARTNER_NAME}.csr.pem" -subj "/OU=${INSTALLATION_TYPE}/O=${PARTNER_NAME}/emailAddress=${EMAIL}/CN=${INSTALLATION_NAME}"
}

verify() {
  if [ $# -eq 1 ]; then
    printf "${GREEN} \r\n"
    printf "usage: verify [client_certificate.pem]"
    printf "${NC} \r\n"
    exit
  fi

  FILE_NAME=$2

  STAGE=$(openssl x509 -noout -subject -in "$FILE_NAME" | grep -o -P '(?<=OU =)(.*)(?=,)' | xargs)
  if [ "$STAGE" = "Com" ]; then
    curl https://cert.lemonbeat.com/certs/LbPlatformComCA.pem --output LbPlatformComCA.pem
    openssl verify -verbose -CAfile LbPlatformComCA.pem -show_chain -untrusted $FILE_NAME $FILE_NAME
  else
    echo "The certificate's stage is ${STAGE}, Only the CA's for Com Stage are published under https://cert.lemonbeat.com/, you need to download the Dev/Test CA certificate verify the certificate manually."
  fi
}

convert_pem_to_p12() {
  if [ $# -eq 1 ]; then
    printf "${GREEN} \r\n"
    printf "usage: convert_pem_to_p12 [client_certificate.pem] [private_key.pem]"
    printf "${NC} \r\n"
    exit
  fi

  CERT=$2
  KEY=$3
  openssl pkcs12 -export -out client_cert.p12 -in $CERT -inkey $KEY
}

convert_p12_to_jks() {
  if [ $# -eq 1 ]; then
    printf "${GREEN} \r\n"
    printf "usage: convert_p12_to_jks [p12 file] [out jks filename] \r\n"
    printf "Note: the certificate will be saved into a jks file under the alias 'client_cert'"
    printf "${NC} \r\n"
    exit
  fi

  P12_FILE=$2
  OUTPUT_FILENAME=$3

  [ -z $4 ] && read -e -p "Enter the password of the p12 file: " P12_PASSWORD || P12_PASSWORD=$4
  [ -z $5 ] && read -e -p "Enter the password for the output jks file: " JSK_PASSWORD || JSK_PASSWORD=$5

  keytool -importkeystore -srckeystore ${P12_FILE} -srcstoretype pkcs12 -srcstorepass ${P12_PASSWORD} -srcalias 1 \
    -destkeystore ${OUTPUT_FILENAME} -deststoretype jks -deststorepass ${JSK_PASSWORD} -destalias client_cert
}

import_trust_store_into_jks() {
  if [ $# -eq 1 ]; then
    printf "${GREEN} \r\n"
    printf "usage: import_trust_store_into_jks [jks file] [trust store] \r\n"
    printf "${NC} \r\n"
    exit
  fi

  [ -z $2 ] && read -e -p "Enter the path to jks file: " JKS_FILE || JKS_FILE=$2
  [ -z $3 ] && read -e -p "Enter the path to trust_store: " TRUST_STORE || TRUST_STORE=$3
  [ -z $4 ] && read -e -p "Enter the password of the jks file: " PASSWORD || PASSWORD=$4

  keytool -import -alias trust_store -file ${TRUST_STORE} -storetype JKS -keystore ${JKS_FILE} -deststorepass ${PASSWORD} -noprompt
}

convert_pem_to_service_client_java_compatible_jks_format() {
  if [ $# -eq 1 ]; then
    printf "${GREEN} \r\n"
    printf "usage: convert_pem_to_service_client_java_compatible_jks_format [p12 file] [out jks filename] \r\n"
    printf "Note: the certificate will be saved into a jks file under the alias 'client_cert', and the root CA certificate under the alias 'trust_store'."
    printf "${NC} \r\n"
    exit
  fi

  [ -z $2 ] && read -e -p "Enter the name of certificate .pem file: " CERT_PEM_FILE || CERT_PEM_FILE=$2
  [ -z $3 ] && read -e -p "Enter the name of your private key .key.pem : " KEY_FILE || KEY_FILE=$3
  [ -z $4 ] && read -e -p "Enter the name of the output jks file: " OUTPUT_JKS_FILENAME || OUTPUT_JKS_FILENAME=$4
  [ -z $5 ] && read -e -p "Enter the password for the private key: " PRIVATE_KEY_PASSWORD || PRIVATE_KEY_PASSWORD=$5

  temp_dir=$(mktemp -d)

  STAGE=$(openssl x509 -noout -subject -in "${CERT_PEM_FILE}" | grep -o -P '(?<=OU =)(.*)(?=,)' | xargs)

  # Converts the key and certificate into p12
  openssl pkcs12 -export -out /${temp_dir}/cert.p12 -in ${CERT_PEM_FILE} -inkey ${KEY_FILE}  -passin pass:${PRIVATE_KEY_PASSWORD} -passout pass:${PRIVATE_KEY_PASSWORD}
  # Import the p12 certificate into a JKS file
  keytool -importkeystore -srckeystore /${temp_dir}/cert.p12 -srcstoretype pkcs12 -srcstorepass ${PRIVATE_KEY_PASSWORD} -srcalias 1 \
    -destkeystore ${OUTPUT_JKS_FILENAME} -deststoretype jks -deststorepass ${PRIVATE_KEY_PASSWORD} -destalias client_cert

  if [ "$STAGE" = "Com" ]; then
    curl https://cert.lemonbeat.com/certs/LbPlatformComCA.pem --output /${temp_dir}/LbPlatformComCA.pem
    # Verify the certificate against the public published CA
    openssl verify -verbose -CAfile /${temp_dir}/LbPlatformComCA.pem -show_chain -untrusted CERT_PEM_FILE CERT_PEM_FILE
    # Import the trust store into the jks file
    keytool -import -alias trust_store -file /${temp_dir}/LbPlatformComCA.pem -storetype JKS -keystore ${OUTPUT_JKS_FILENAME} -deststorepass ${PRIVATE_KEY_PASSWORD} -noprompt
  else
    echo "The certificate's stage is ${STAGE}, Only the CA's for Com Stage are published under https://cert.lemonbeat.com/ and can be download. You need to download the DEV/Test certificate and run the 'verify' 'import_trust_store_into_jks' steps manually."
  fi

  rm -R ${temp_dir}
}

if [ $# -eq 0 ]; then
  help
elif [ $1 = "verify" ]; then
  verify "$@"
elif [ $1 = "create_csr" ]; then
  create_csr "$@"
elif [ $1 = "convert_pem_to_p12" ]; then
  convert_pem_to_p12 "$@"
elif [ $1 = "convert_p12_to_jks" ]; then
  convert_p12_to_jks "$@"
elif [ $1 = "import_trust_store_into_jks" ]; then
  import_trust_store_into_jks "$@"
elif [ $1 = "convert_pem_to_service_client_java_compatible_jks_format" ]; then
  convert_pem_to_service_client_java_compatible_jks_format "$@"
else
  printf "${RED} \r\n"
  printf "wrong argument $1 \r\n"
  printf "${NC} \r\n"
  help
fi
