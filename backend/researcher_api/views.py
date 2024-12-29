import os
import json
import requests
from rest_framework import views, status, permissions
from rest_framework.response import Response
from rest_framework import serializers
from .models import Article
from django.conf import settings
import re
import hashlib
from bs4 import BeautifulSoup


class SciHubView(views.APIView):

    def __init__(self):
         self.sess = requests.Session()
         self.sess.headers = {'User-Agent': 'Mozilla/5.0 (X11; Linux x86_64; rv:27.0) Gecko/20100101 Firefox/27.0'}
         self.available_base_url_list = self._get_available_scihub_urls()
         self.base_url = self.available_base_url_list[0] + '/'

    def set_proxy(self, proxy):
         if proxy:
             self.sess.proxies = {
                "http": proxy,
                "https": proxy, }

    def _change_base_url(self):
        if not self.available_base_url_list:
            raise Exception('Ran out of valid sci-hub urls')
        del self.available_base_url_list[0]
        self.base_url = self.available_base_url_list[0] + '/'
        # logger.info("I'm changing to {}".format(self.available_base_url_list[0])) #remove logging

    def _get_available_scihub_urls(self):
        urls = []
        res = requests.get('https://sci-hub.now.sh/')
        s = self._get_soup(res.content)
        for a in s.find_all('a', href=True):
             if 'sci-hub.' in a['href']:
                 urls.append(a['href'])
        return urls
    def search(self, query, limit=10, download=False):
         start = 0
         results = {'papers': []}
         while True:
             try:
                res = self.sess.get('https://scholar.google.com/scholar', params={'q': query, 'start': start})
             except requests.exceptions.RequestException as e:
                results['err'] = 'Failed to complete search with query %s (connection error)' % query
                return results

             s = self._get_soup(res.content)
             papers = s.find_all('div', class_="gs_r")

             if not papers:
                if 'CAPTCHA' in str(res.content):
                    results['err'] = 'Failed to complete search with query %s (captcha)' % query
                return results

             for paper in papers:
                if not paper.find('table'):
                    source = None
                    pdf = paper.find('div', class_='gs_ggs gs_fl')
                    link = paper.find('h3', class_='gs_rt')

                    if pdf:
                         source = pdf.find('a')['href']
                    elif link.find('a'):
                         source = link.find('a')['href']
                    else:
                        continue

                    results['papers'].append({
                         'name': link.text,
                          'url': source
                     })

                    if len(results['papers']) >= limit:
                        return results

             start += 10

    def download(self, identifier, destination='', path=None):
         data = self.fetch(identifier)

         if not 'err' in data:
             self._save(data['pdf'],
                       os.path.join(destination, path if path else data['name']))

         return data

    def fetch(self, identifier):
         try:
            url = self._get_direct_url(identifier)
            res = self.sess.get(url, verify=False)

            if res.headers['Content-Type'] != 'application/pdf':
                self._change_base_url()
                raise Exception('Failed to fetch pdf with identifier %s '
                                           '(resolved url %s) due to captcha' % (identifier, url))
            else:
                return {
                    'pdf': res.content,
                    'url': url,
                    'name': self._generate_name(res)
                }

         except requests.exceptions.ConnectionError:
            self._change_base_url()
         except requests.exceptions.RequestException as e:
             return {
                'err': 'Failed to fetch pdf with identifier %s (resolved url %s) due to request exception.'
                       % (identifier, url)
            }

    def _get_direct_url(self, identifier):
         id_type = self._classify(identifier)
         return identifier if id_type == 'url-direct' \
             else self._search_direct_url(identifier)
    def _search_direct_url(self, identifier):
        res = self.sess.get(self.base_url + identifier, verify=False)
        s = self._get_soup(res.content)
        iframe = s.find('iframe')
        if iframe:
            return iframe.get('src') if not iframe.get('src').startswith('//') \
                else 'http:' + iframe.get('src')
    def _classify(self, identifier):
         if (identifier.startswith('http') or identifier.startswith('https')):
            if identifier.endswith('pdf'):
                return 'url-direct'
            else:
                return 'url-non-direct'
         elif identifier.isdigit():
            return 'pmid'
         else:
            return 'doi'

    def _save(self, data, path):
         with open(path, 'wb') as f:
             f.write(data)

    def _get_soup(self, html):
         return BeautifulSoup(html, 'html.parser')

    def _generate_name(self, res):
        name = res.url.split('/')[-1]
        name = re.sub('#view=(.+)', '', name)
        pdf_hash = hashlib.md5(res.content).hexdigest()
        return '%s-%s' % (pdf_hash, name[-20:])
    permission_classes = [permissions.AllowAny]

    def post(self, request, *args, **kwargs):

          identifier = request.data.get('identifier') # identifier = doi / url etc
          if not identifier:
              return Response({"error":"Identifier required"}, status=status.HTTP_400_BAD_REQUEST)

          result = self.download(identifier) # gets the pdf using sci hub
          if 'err' in result :
            return Response( {"error" : "Fail fetching /download article from identifier"} , status=status.HTTP_400_BAD_REQUEST)

          return Response(  { "url":result.get('url'), "name": result.get('name') }  ,status=status.HTTP_200_OK) # if result is ok, then, return json data object
