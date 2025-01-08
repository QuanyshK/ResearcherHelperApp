import os
from google.oauth2 import service_account

SCOPES = ['https://www.googleapis.com/auth/cloud-platform']

def get_google_credentials():
    credentials_path = os.getenv('GOOGLE_APPLICATION_CREDENTIALS', 'credentials.json')
    credentials = service_account.Credentials.from_service_account_file(
        credentials_path, scopes=SCOPES
    )
    return credentials