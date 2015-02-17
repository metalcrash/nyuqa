# nyuqa
A close domain question answering system focuses on NYU

Project description:

This is a Question Answering System on a closed domain of NYU facts. It's developed as web application using Java Servlet. The question classifier and document indexer are implemented using OpenNLP model and resource. The main module, information retrieval part is Solr. Question classifier and indexer are integrated in it as plugins.

I'll illustrate the program structure with the process of its execution:

User input question on input bar of web page(html+JSP), then the question is sent to back end program(Java servlet). Received this question, then send a query request to Solr with this question. The question classifier plugin(a jar) of Solr will parse the question to several keywords(question type and query terms). After Solr get the query results(a set of documents), it will send them as a response to Servlet. Once Servlet get this response, it will select the top-n results and parse them. Then it will apply a more specific ranking algorithm to those documents base on keyword density, and finally choose the top-3 paragraph send to front-end.
Additionally, documents(the corpus) used in Solr need to be indexed before query. The indexer plugin(a jar) of Solr is for this.

File structure:
customClassifier: The source code of classifier plugin. Libraries are located in the "lib" directory at root directory.
customIndexer: The source code of indexer plugin. Libraries are located in the "lib" directory at root directory.
lib: The libraries that project depends.
qaservlet: The Servlet program and web page, add the compiled files as web application in your server.

The Solr configuration isn't provide because it's quite similar to the default one "collection 1" except two plugins.
