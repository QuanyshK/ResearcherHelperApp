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

        if pdf_embed:
            pdf_src = pdf_embed['src']
            full_pdf_url = f"https:{pdf_src}" if pdf_src.startswith("//") else pdf_src
            return scihub_url, full_pdf_url
        
        return scihub_url, None 
    
    except requests.RequestException as e:
        return None, None

@api_view(['POST'])
def generate_scihub_link(request):
    doi = request.data.get('doi')
    
    if not doi:
        return Response({"error": "DOI is required"}, status=status.HTTP_400_BAD_REQUEST)
    
    article, created = Article.objects.get_or_create(doi=doi)
    
    if not article.hacked_link or not article.pdf_link:
        hacked_link, pdf_link = get_scihub_pdf_link(doi)
        if hacked_link:
            article.hacked_link = hacked_link
            article.pdf_link = pdf_link  # Сохраняем pdf_link
            article.save()
        else:
            return Response({"error": "Failed to retrieve PDF link"}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)
    
    return Response({
        "id": article.id,
        "doi": article.doi,
        "hacked_link": article.hacked_link,
        "pdf_link": article.pdf_link  # Возвращаем pdf_link в ответ
    })
