function main(env, args) {
  var astore = 'paesia';
  try {
    var categories = grabCategory(astore, env);
    var map = env.newHashMap();
    for (var i = 0; i < categories.size(); i++) {
      var cat = categories.get(i);
      map.put(cat.get('node'), cat);
    }
    for (var i = 0; i < categories.size(); i++) {
      var cat = categories.get(i);
      var node = cat.get('node');
      var title = cat.get('title');
      var parent = cat.get('parent');
      saveCategory(title, node, parent, env);
    }
  } catch (e) {
    env.error(e);
  }
}

function saveCategory(title, node, parent, env) {
  if (findCategoryByNode(node, env)) return;
  var schema = 's|node|s|title|s|parent';
  var entity = env.newEntity();
  entity.setSchema(schema);
  entity.setKind('Category_Amazon');
  entity.setId(env.uniqid());
  entity.setString('node', node);
  entity.setString('title', title);
  entity.setString('parent', parent);
  entity.save();
}

function findCategoryByNode(node, env) {
  var entity = env.newEntity();
  var query = entity.newTermQuery(entity.newTerm('node', node));
  var size = entity.count('Category_Amazon', query, 1);
  return (size > 0);
}

function grabCategory(astore, env) {
  var tag = env.newArrayList();
  try {
    var nodelist = env.newArrayList();
    var alink = env.newURL('http://astore.amazon.com/' + astore + '-20');
    var doc = env.newJsoup().parse(alink, 60000);
    var elements = doc.select('#searchbrowse a');
    for (var i = 0; i < elements.size(); i++) {
      var element = elements.get(i);
      var title = element.text();
      var url = element.attr('href');
      var pos = url.lastIndexOf('node=');
      if (pos < 0) continue;
      var node = url.substring(pos + 5);
      pos = node.indexOf('&');
      if (pos >= 0) {
        node = node.substring(0, pos);
      }
      var item = env.newHashMap();
      item.put('title', title);
      item.put('node', node);
      item.put('parent', '');
      tag.add(item);
      nodelist.add(node);
      env.info(node + ' : ' + title);
    }
    var no = 0;
    while (no < nodelist.size()) {
      alink = env.newURL('http://astore.amazon.com/' + astore + '-20?node=' + nodelist.get(no));
      doc = env.newJsoup().parse(alink, 60000);
      elements = doc.select('#searchbrowse .indent a');
      for (var i = 0; i < elements.size(); i++) {
        var element = elements.get(i);
        var title = element.text();
        var url = element.attr('href');
        var pos = url.lastIndexOf('node=');
        if (pos < 0) continue;
        var node = url.substring(pos + 5);
        pos = node.indexOf('&');
        if (pos >= 0) {
          node = node.substring(0, pos);
        }
        if (nodelist.indexOf(node) >= 0) continue;
        var item = env.newHashMap();
        item.put('title', title);
        item.put('node', node);
        item.put('parent', nodelist.get(no));
        tag.add(item);
        nodelist.add(node);
        env.info(node + ' : ' + title);
      }
      no++;
    }
  } catch (e) {
    env.error(e);
  }
  return tag;
}