function main(env, args) {
  var astore = 'paesia';
  var node = '';
  var frompage = 1;
  var topage = 10000;
  var batch = 5;
  var cache = true;
  if (node.length == 0) {
    if (!cache) {
      clearCategoryMarks(env);
    }
    var nodelist = loadCategories(env);
    for (var i = 0; i < nodelist.size(); i++) {
      node = nodelist.get(i);
      for (var no = frompage; no <= topage; no += batch) {
        var min = no;
        var max = no + batch - 1;
        if (max > topage) max = topage;
        var products = grabProduct(astore, node, min, max, env);
        if (products.size() == 0) break;
        for (var i = 0; i < products.size(); i++) {
          var pro = products.get(i);
          saveProduct(pro, env);
        }
        env.info('Saved: ' + products.size());
      }
      env.info('Saved all from category: ' + node);
      markCategory(node, env);
    }
  } else {
    for (var no = frompage; no <= topage; no += batch) {
      var min = no;
      var max = no + batch - 1;
      if (max > topage) max = topage;
      var products = grabProduct(astore, node, min, max, env);
      if (products.size() == 0) break;
      for (var i = 0; i < products.size(); i++) {
        var pro = products.get(i);
        saveProduct(pro, env);
      }
      env.info('Saved: ' + products.size());
    }
  }
}

function clearCategoryMarks(env) {
  var entity = env.newEntity();
  var results = entity.search('Category_Amazon', entity.newMatchAllDocsQuery(), java.lang.Integer.MAX_VALUE);
  for (var i = 0; i < results.size(); i++) {
    results.get(i).setMark('');
    results.get(i).save();
  }
}

function markCategory(node, env) {
  var cat = env.newEntity();
  var results = cat.search('Category_Amazon', cat.newTermQuery(cat.newTerm('node', node)), 1);
  if (results.size() == 0) return;
  cat = results.get(0);
  cat.setMark('crawled');
  cat.save();
}

function loadCategories(env) {
  var tag = env.newArrayList();
  var entity = env.newEntity();
  var results = entity.search('Category_Amazon', entity.newMatchAllDocsQuery(), java.lang.Integer.MAX_VALUE);
  for (var i = 0; i < results.size(); i++) {
    if (results.get(i).getMark() == 'crawled') continue;
    tag.add(results.get(i).getString('node'));
  }
  return tag;
}

function saveLink(title, url, desc, env) {
  if (findLinkByUrl(url, env)) return;
  var schema = 's|url|a|title|a|desc';
  var entity = env.newEntity();
  entity.setSchema(schema);
  entity.setKind('Link');
  entity.setId(env.uniqid());
  entity.setString('url', url);
  entity.setString('title', title);
  entity.setString('desc', desc);
  entity.save();
}

function findLinkByUrl(url, env) {
  var entity = env.newEntity();
  var query = entity.newTermQuery(entity.newTerm('url', url));
  var size = entity.count('Link', query, 1);
  return (size > 0);
}

function saveProduct(pro, env) {
  var title = pro.get('title');
  var url = pro.get('url');
  if (title == null || title.length == 0 || url == null || url.length == 0) return;
  var desc = pro.get('description') + '';
  if (desc == null) desc = '';
  if (desc.length > 0) {
    var doc = env.newJsoup().parse(desc);
    desc = doc.select('body').first().text();
  }
  saveLink(title, url, desc, env);
}

function grabProduct(astore, node, frompage, topage, env) {
  var tag = env.newArrayList();
  for (var no = frompage; no <= topage; no++) {
    try {
      var alink = env.newURL('http://astore.amazon.com/' + astore + '-20?node=' + node + '&page=' + no);
      var doc = env.newJsoup().parse(alink, 60000);
      var elements = doc.select('#featuredProducts .textrow a');
      var map = env.newHashMap();
      for (var i = 0; i < elements.size(); i++) {
        var element = elements.get(i);
        var title = element.text();
        var url = element.attr('href');
        var pos = url.lastIndexOf('/detail/');
        if (pos < 0) continue;
        var code = url.substring(pos + 8);
        var url = env.newURL(alink, url) + '';
        var item = env.newHashMap();
        item.put('code', code);
        item.put('title', title);
        item.put('url', url);
        map.put(code, item);
      }
      elements = doc.select('#featuredProducts .imagerow a');
      for (var i = 0; i < elements.size(); i++) {
        var element = elements.get(i);
        var url = element.attr('href');
        var pos = url.lastIndexOf('/detail/');
        if (pos < 0) continue;
        var code = url.substring(pos + 8);
        var item = map.get(code);
        if (item == null) continue;
        var child = element.select('img').first();
        if (child == null) continue;
        var title = child.attr('alt');
        var smimg = child.attr('src');
        if (title.length() > 0) {
          item.put('title', title);
        }
        item.put('small-image', smimg);
      }

      var keys = env.getKeys(map);
      for (var i = 0; i < keys.size(); i++) {
        try {
          var item = map.get(keys.get(i));
          alink = env.newURL(item.get('url'));
          doc = env.newJsoup().parse(alink, 60000);
          var element = doc.select('#detailImage img').first();
          if (element != null) {
            item.put('large-image', element.attr('src'));
          }
          element = doc.select('#productDescription').first();
          if (element != null) {
            var desc = element.html();
            var pattern = '<h2>Product Description</h2>';
            var pos = desc.indexOf(pattern);
            if (pos >= 0) {
              desc = desc.substring(pos + pattern.length);
            }
            var bdoc = env.newJsoup().parse(desc, item.get('url'));
            buildURL(bdoc, item.get('url'), env);
            desc = bdoc.select('body').first().html();
            if (desc.indexOf('<html') < 0) {
              item.put('description', desc);
            }
          }
          element = doc.select('#productDetails').first();
          if (element != null) {
            var desc = element.html();
            var pattern = '<h2>Product Details</h2>';
            var pos = desc.indexOf(pattern);
            if (pos >= 0) {
              desc = desc.substring(pos + pattern.length);
            }
            var bdoc = env.newJsoup().parse(desc, item.get('url'));
            buildURL(bdoc, item.get('url'), env);
            desc = bdoc.select('body').first().html();
            if (desc.indexOf('<html') < 0) {
              item.put('details', desc);
            }
          }
          element = doc.select('#editorialReviews').first();
          if (element != null) {
            var desc = element.html();
            var bdoc = env.newJsoup().parse(desc, item.get('url') + '');
            buildURL(bdoc, item.get('url'), env);
            desc = bdoc.select('body').first().html();
            if (desc.indexOf('<html') < 0) {
              item.put('editorial-reviews', desc);
            }
          }
          element = doc.select('#detailListPrice').first();
          if (element != null) {
            item.put('list-price', element.text());
          }
          element = doc.select('#detailOfferPrice').first();
          if (element != null) {
            item.put('offer-price', element.text());
          }
          element = doc.select('#addToCartForm a').first();
          if (element != null) {
            item.put('buy-url', element.attr('href'));
          }
          env.info(node + ' : ' + no + ' : ' + (i + 1) + ' : ' + item.get('url'));
        } catch (e) {
          env.error(e);
        }
      }

      for (var i = 0; i < keys.size(); i++) {
        tag.add(map.get(keys.get(i)));
      }
    } catch (e) {
      env.error(e);
    }
  }
  return tag;
}

function buildURL(doc, baseUrl, env) {
  baseUrl = env.newURL(baseUrl);
  var elements = doc.select('a');
  for (var i = 0; i < elements.size(); i++) {
    var element = elements.get(i);
    var url = env.newURL(baseUrl, element.attr('href'));
    element.attr('href', url + '');
  }
  elements = doc.select('img');
  for (var i = 0; i < elements.size(); i++) {
    var element = elements.get(i);
    var url = env.newURL(baseUrl, element.attr('src'));
    element.attr('src', url + '');
  }
}