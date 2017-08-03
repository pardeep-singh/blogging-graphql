# Sample Blogging APP using GraphQL and Compojure.

This project provides the sample blogging app to try [GraphQL](https://www.graphql.com/) with [Lacinia](http://lacinia.readthedocs.io/en/latest/) and [Compojure](https://github.com/weavejester/compojure) in Clojure.

## Prerequisites

To run this project, [Java 8](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html) and [Leiningen](https://leiningen.org/) needs to be installed.

## Running

1. Build the jar
```
	lein uberjar
```

2. Start the GraphQL server
```
	java -jar target/blogging-graphql-0.1.0-standalone.jar
```

3. Open GraphiQL IDE using http://localhost:7000/graphiql in your browser.

## Using GraphiQL IDE

1. Queries - Fetch Resources
```
{
	blogs: getBlogs {
		id
		author {
			id
		}
		comments {
    		body
    	}
  	}
  	authors: getAllAuthors {
    	id
    	email
    	name
  	}
}
```

2. Mutations - Create or Update Resources
```
mutation {
	createAuthor(email: "random@mail.com", name: "Random Author") {
		id
		name
		email
	}
}
```

3. Fragments - Reusable Units
```
fragment author_fields on author {
		id
		email
		name
}
fragment comment_fields on comment {
		id
		body
		author {
			...author_fields
		}
}
fragment blog_fields on blog {
		id
		body
		author {
			...author_fields
		}
		comments {
			...comment_fields
		}
}
{
		getBlogs {
			...blog_fields
		}
}
```

4. Variables - Pass Dynamic Values to Queries.<br>
Pass below as query
```
query queries($blogID: String!, $first: Int!, $skip: Int!) {
	blog: getBlog(id: $blogID) {
		id
		comments(first: $first) {
			id
		}
  	}
  authors: getAllAuthors {
  		id
  	}
  	blogs: getBlogs(first: $first) {
  		id
  		comments {
  			id
  		}
  	}
  comments: getComments(blogID: $blogID, first: $first, skip: $skip) {
  		id
  	}
}
```
and this as a query variables(change the blogID value accordingly)
```
{
		"blogID": "40bcb0b5-fe8b-4486-af4a-5f2f395ca75c",
		"first": 5,
		"skip": 0
}
```

5. Schema Introspection - Inspect Schema
```
{
	__schema{
		types{
			name
			description
		}
    	queryType{
      		name
      		description
      		fields{
        		name
        		description
      		}
    	}
    	mutationType{
      		name
      		description
      		fields{
        		name
        		description
      		}
    	}
  	}
}
```
