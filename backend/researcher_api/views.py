import requests
from bs4 import BeautifulSoup
from rest_framework.decorators import api_view
from rest_framework.response import Response
from rest_framework import status
from .models import Article
from .serializers import ArticleSerializer

SCIHUB_DOMAINS = [
    "https://sci-hub.ru",
    "https://sci-hub.wf",
    "https://sci-hub.se",
    "https://sci-hub.st"
]

def get_scihub_pdf_link(doi):
    for domain in SCIHUB_DOMAINS:
        scihub_url = f"{domain}/{doi}"
        
        try:
            response = requests.get(scihub_url, timeout=10)
            response.raise_for_status()
            
            soup = BeautifulSoup(response.content, 'html.parser')

            pdf_embed = soup.find('embed', {'type': 'application/pdf'})
            pdf_src = pdf_embed['src'] if pdf_embed else None
            full_pdf_url = f"https:{pdf_src}" if pdf_src and pdf_src.startswith("//") else pdf_src

            citation = soup.find('div', {'id': 'citation'})
            title = citation.find('i').text if citation else "Title not found"

            return scihub_url, full_pdf_url, title
        
        except requests.RequestException:
            continue

    return None, None, None

@api_view(['POST'])
def generate_scihub_link(request):
    doi = request.data.get('doi')
    
    if not doi:
        return Response({"error": "DOI is required"}, status=status.HTTP_400_BAD_REQUEST)

    article, created = Article.objects.get_or_create(doi=doi)
    
    if not article.hacked_link or not article.pdf_link or not article.title:
        hacked_link, pdf_link, title = get_scihub_pdf_link(doi)
        if hacked_link:
            article.hacked_link = hacked_link
            article.pdf_link = pdf_link
            article.title = title
            article.save()
        else:
            return Response(
                {"error": "Failed to retrieve PDF link from all Sci-Hub domains"},
                status=status.HTTP_500_INTERNAL_SERVER_ERROR
            )
    
    return Response({
        "id": article.id,
        "doi": article.doi,
        "title": article.title,
        "hacked_link": article.hacked_link,
        "pdf_link": article.pdf_link
    })

@api_view(['GET'])
def get_articles(request):
    articles = Article.objects.all().order_by('-id')[:5]
    serializer = ArticleSerializer(articles, many=True)
    return Response(serializer.data)
