<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="text" encoding="UTF-8"/>


<xsl:template match="/">
<xsl:apply-templates select="PubmedArticle/MedlineCitation/Article"/>
</xsl:template>


<xsl:template match="PubmedArticle/MedlineCitation/Article">
@Article{<xsl:value-of select="AuthorList/Author/LastName"/><xsl:value-of select="Journal/JournalIssue/PubDate/Year"/>,
  author =       {<xsl:apply-templates select="AuthorList/Author"/>},
  title =        {<xsl:value-of select="ArticleTitle"/>},
  journal =      {<xsl:value-of select="../MedlineJournalInfo/MedlineTA"/>},
  year =         {<xsl:value-of select="Journal/JournalIssue/PubDate/Year"/>},
  volume =       {<xsl:value-of select="Journal/JournalIssue/Volume"/>},
  number =       {<xsl:value-of select="Journal/JournalIssue/Issue"/>},
  pages =        {<xsl:value-of select="Pagination/MedlinePgn"/>}
}
</xsl:template>

<xsl:template match="AuthorList/Author"><xsl:if test="position()!=1"> and </xsl:if> <xsl:apply-templates select="Initials"/>. <xsl:apply-templates select="LastName"/></xsl:template>


</xsl:stylesheet>
