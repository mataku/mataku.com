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

            var tagsContainer = document.createElement('div');
            tagsContainer.className = 'tags';
            if (article.tags && article.tags.length > 0) {
                article.tags.forEach(function (tag) {
                    var tagSpan = document.createElement('span');
                    tagSpan.className = 'tag';
                    tagSpan.textContent = tag;
                    tagsContainer.appendChild(tagSpan);
                });
            }

            card.appendChild(title);
            card.appendChild(date);
            card.appendChild(tagsContainer);
            list.appendChild(card);
        });
    });
