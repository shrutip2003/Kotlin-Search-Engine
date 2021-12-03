package websearch

import java.util.*

class SearchEngine(val map: Map<URL, WebPage>) {
  var index: Map<String, List<SearchResult>> = mapOf()

  fun compileIndex() {
    val list = mutableListOf<Pair<String, URL>>()  // list of (word, url)
    for (entry in map) {
      val words = entry.value.extractWords()
      val url = entry.key
      for (word in words) {
        list.add(Pair(word, url))
      }
    }

    // SHUFFLING STEP
    // list of the form [(word,url)] :: word x appears in page url y]
    // list.groupBy of the form {word = [(word,url)] } -- word mapped to list of (word,url) pairs
    // values are a list of (string, url)
    // to covert list of (string, url) to list of url

    // mapvalues  (grouped.mapValues { entry -> entry.value.map{it.map{it.second}}} )kept giving a type error, hence had to transform the values separately and zip them with the words to create a new map
    val grouped: Map<String, List<Pair<String, URL>>> = list.groupBy { x -> x.first }
    val words = grouped.keys
    val values: Collection<List<Pair<String, URL>>> = grouped.values
    val new = values.map { it.map { it.second } }
    val wordsWithListOFUrl: Map<String, List<URL>> = words.zip(new).toMap()
    // wordsWithListOfUrl is a map between word , and a list of urls that mention the word
    val wordsWithRankedURL = wordsWithListOFUrl.mapValues { entry -> rank(entry.value) }
    index = wordsWithRankedURL
  }

  fun rank(listUrl: List<URL>): List<SearchResult> {
    val list = mutableListOf<SearchResult>()
    for (url in listUrl.distinct()) {
      val num = Collections.frequency(listUrl, url) // count of url in listUrl
      list.add(SearchResult(url, num))
    }
    return list.sortedBy { it.numRefs }.reversed()
  }

  fun searchFor(query: String): SearchResultsSummary {
    val results: List<SearchResult>? = index.get(query)

    // gets executed if result is not null
    results?.let {
      return SearchResultsSummary(query, results)
    }
      // gets executed if result is null
      ?: run {
        val emptyResults    = listOf<SearchResult>()
        return SearchResultsSummary(query, emptyResults)
      }
  }
}

class SearchResult(val url: URL, val numRefs: Int) {
  override fun toString(): String = "\t $url - $numRefs references"
}

class SearchResultsSummary(val query: String, val results: List<SearchResult>) {

  override fun toString(): String {
    val final = StringBuilder("Results for \"$query\": \n")
    if (results.isNotEmpty()) {
      for (result in results) {
        final.append(result.toString() + "\n")
      }
    } else {
      final.append("\t No search results available for $query")
    }
    return final.toString()
  }
}

// to test the indexer
/*
fun main(){
  val docHomePageHtml =
    """<html>
         <head>
           <title>Department of Computing</title>
         </head>
           <body>
             <p>Welcome to the Department of Computing at <a href="https://www.imperial.ac.uk">Imperial College London</a>.</p>
           </body>
        </html>"""

  val imperialHomePageHtml =
    """<html>
         <head>
           <title>Imperial College London</title>
         </head>
           <body>
             <p>Imperial people share ideas, expertise and technology to find answers to the big scientific questions and tackle global challenges</p>
             <p>See the latest news about our research on the <a href="https://www.bbc.co.uk/news">BBC</a></p>
           </body>
        </html>"""

  val bbcNewsPageHtml =
    """<html>
         <head>
           <title>BBC News</title>
         </head>
           <body>
             <p>Here is all the latest news about science and other things.</p>
           </body>
        </html>"""

   val docHomePage = WebPage(Jsoup.parse(docHomePageHtml))
   val imperialHomePage = WebPage(Jsoup.parse(imperialHomePageHtml))
   val bbcNews = WebPage(Jsoup.parse(bbcNewsPageHtml))

   val downloadedPages = mapOf(
    URL("https://www.imperial.ac.uk/computing") to docHomePage,
    URL("https://www.imperial.ac.uk") to imperialHomePage,
    URL("https://www.bbc.co.uk/news") to bbcNews
  )
  val searchEngine = SearchEngine(downloadedPages)

  searchEngine.compileIndex()
  println(searchEngine.searchFor("of"))

}

 */
