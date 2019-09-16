"""
    This script accesses properties from Azure Key Vault.
"""

import sys
from azure.keyvault import KeyVaultClient
from msrestazure.azure_active_directory import MSIAuthentication

if len(sys.argv) != 3:
  print('Need to provide parameter path and outputfile arguments')
  print('Example: python GetSecretsFromKeyvault.py ' +
        '"/applicationName/dev/" "application.properties"')
  exit(1)
CONTEXT = sys.argv[1]
OUTPUTFILE = sys.argv[2]


def get_key_vault_credentials():
  return MSIAuthentication(resource='https://vault.azure.net')

credentials = get_key_vault_credentials()
client = KeyVaultClient(credentials)

def get_secrets_by_path(path):
  secrets = client.get_secrets(path)
  secret_id_list = []
  for secret in secrets:
    secret_id_list.append(secret.id)

  return secret_id_list

def get_secret_object(keyvault_url, secret_name, secret_version):
  secret = client.get_secret(keyvault_url, secret_name , secret_version)
  print(secret)

def get_secret_tuples_with_stripped_context_prefixes(secrets):
  for secret in secrets:
    test = strip_secret_name(secret)
    yield (test, get_secret_value(test))

def strip_secret_name(secret_name):
  full_context = CONTEXT + "/secrets/"
  if secret_name.startswith(full_context):
    return secret_name[len(full_context):]

def get_secret_value(name):
  secret = client.get_secret(CONTEXT, name, "")
  return secret.value

secrets = get_secrets_by_path(CONTEXT)
properties = get_secret_tuples_with_stripped_context_prefixes(secrets)

property_names = []
with open(OUTPUTFILE, 'w') as f:
  for prop in properties:
    f.write(prop[0] + '=' + prop[1] + '\n')
    property_names.append(prop[0])

print(property_names)
print('Wrote the following properties to ' + OUTPUTFILE + ': ' +
      ", ".join([x for x in property_names]))

exit(0)
