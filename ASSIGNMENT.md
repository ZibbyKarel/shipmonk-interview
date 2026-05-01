# Assignment

Use this application skeleton to implement an exchange rates provider for our other services. The first version of the application will be implemented as a caching layer over a 3rd-party service.

https://github.com/shipmonk-rnd/exchange-rates-task-stub

We don't want you to spend days with this task, therefore if you'll have to decide between finishing all functionality or designing app architecture, give the architecture a priority. Nicely architected application, but not fully functional will get more points than badly architected but working app.

- Use https://fixer.io/ as exchange rates source
- Register a new account for development and testing on your personal email
- Base currency is USD
- Response shape of /api/v1/rates/{day} should be similar to what https://fixer.io/documentation responds
- Use PostgreSQL to store the cached exchange rates
- Start a local database for development using docker-compose up -d database
- You can use whatever libraries you may need
- If you have experience with Hibernate, please use it to demonstrate your knowledge
- The service will be used only internally, so securing the API is not needed
- Prioritize making the code testable, don’t spend too much time writing tests for everything at the expense of other requirements. But if you want and have time for it, you can write a test or two.
- Think of edge cases that can occur in this environment and design solid error handling so that the downstream services can be simplified as much as reasonably possible.
- See test.sh for example requests

Please, don't push anything to the repo and don't fork it. Use your own GIT repo.
