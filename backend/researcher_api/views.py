import requests
from bs4 import BeautifulSoup
from rest_framework.decorators import api_view
from rest_framework.response import Response
from rest_framework import status
from .models import Article
from .serializers import ArticleSerializer

def get_scihub_pdf_link(doi):
    scihub_url = f"https://sci-hub.ru/{doi}"
    
    try:
        response = requests.get(scihub_url)
        response.raise_for_status()
        
        soup = BeautifulSoup(response.content, 'html.parser')
        
        pdf_embed = soup.find('embed', {'type': 'application/pdf'})
        pdf_iframe = soup.find('iframe')
        pdf_object = soup.find('object', {'type': 'application/pdf'})

        for element in [pdf_embed, pdf_iframe, pdf_object]:
            if element:
                pdf_src = element.get('src')
                full_pdf_url = f"https:{pdf_src}" if pdf_src.startswith("//") else pdf_src
                return full_pdf_url
        
        return None
    
    except requests.RequestException as e:
        return None

@api_view(['POST'])
def generate_scihub_link(request):
    doi = request.data.get('doi')
    
    if not doi:
        return Response({"error": "DOI is required"}, status=status.HTTP_400_BAD_REQUEST)
    
    article, created = Article.objects.get_or_create(doi=doi)
    
    if not article.hacked_link:
        pdf_link = get_scihub_pdf_link(doi)
        if pdf_link:
            article.hacked_link = pdf_link
            article.pdf_url = pdf_link
            article.save()
        else:
            return Response({"error": "Failed to retrieve PDF link"}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)
    
    serializer = ArticleSerializer(article)
    return Response(serializer.data)
