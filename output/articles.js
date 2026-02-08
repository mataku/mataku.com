fetch('/articles.json')
    .then(function (response) { return response.json(); })
    .then(function (articles) {
        var list = document.getElementById('article-list');
        articles.forEach(function (article) {
            var card = document.createElement('a');
            card.href = article.path;
            card.className = 'article-card';

            var title = document.createElement('h2');
            title.textContent = article.title;

            var date = document.createElement('time');
            date.textContent = article.date;

            card.appendChild(title);
            card.appendChild(date);
            list.appendChild(card);
        });
    });
