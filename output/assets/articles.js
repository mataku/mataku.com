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

            if (article.tags && article.tags.length > 0) {
                var tagsContainer = document.createElement('div');
                tagsContainer.className = 'tags';
                article.tags.forEach(function (tag) {
                    var tagElement = document.createElement('span');
                    tagElement.className = 'tag';
                    tagElement.textContent = tag;
                    tagsContainer.appendChild(tagElement);
                });
                card.appendChild(tagsContainer);
            }

            list.appendChild(card);
        });
    });