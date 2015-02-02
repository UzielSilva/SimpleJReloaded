/*
 * _builtin_utils.sj
 * Copyright (C) 2005,2006 Gerardo Horvilleur Martinez
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */

final Stack() {
  final type = "Stack";
  
  var top = null;
  
  final isEmpty() {
    return top == null;
  }

  final removeAll() {
    top = null;
  }
  
  final push(x) {
    top = {value: x, next: top};
  }

  final put = push;
  
  final pop() {
    if (top == null)
      error("pop: Stack is empty");
    var v = top.value;
    top = top.next; 
    return v;
  }

  final get = pop;

  final contains(element) {
    for (var ptr = top; ptr != null; ptr = ptr.next)
      if (ptr.value == element)
        return true;
    return false;
  }

  final size() {
    var count = 0;
    for (var ptr = top; ptr != null; ptr = ptr.next)
      count++;
    return count;
  }

  final toArray() {
    var result = new array[size()];
    for (var ptr = top, i = 0; ptr != null; ptr = ptr.next)
      result[i++] = ptr.value;
    return result;
  }
  
  final putAll(other) {
    if (!isArray(other)) {
      if (!isCollection(other))
        error("putAll: Not an array or collection");
      other = other.toArray();
    }
    var n = length(other);
    for (var i = n - 1; i >= 0; i--)
      push(other[i]);
  }
  
  final createWith(other) {
    var r = Stack();
    r.putAll(other);
    return r;
  }
  
  return this;
}

final isStack(s) {
  return isEnv(s) && hasName("type", s) && s.type == "Stack";
}

final Queue() {
  final type = "Queue";
  
  var head = {next: null};
  
  var tail = head;
  
  final isEmpty() {
    return head == tail;
  }

  final removeAll() {
    head = tail = {next: null};
  }
  
  final put(x) {
    tail = tail.next = {value: x, next: null};
  }
  
  final get() {
    if (head == tail)
      error("get: Queue is empty");
    return (head = head.next).value;
  }

  final contains(element) {
    for (var ptr = head.next; ptr != null; ptr = ptr.next)
      if (ptr.value == element)
        return true;
    return false;
  }

  final size() {
    var count = 0;
    for (var ptr = head.next; ptr != null; ptr = ptr.next)
      count++;
    return count;
  }

  final toArray() {
    var result = new array[size()];
    if (length(result) == 0)
      return result;
    for (var ptr = head.next, var i = 0; ptr != null; ptr = ptr.next)
      result[i++] = ptr.value;
    return result;
  }
  
  final putAll(other) {
    if (!isArray(other)) {
      if (!isCollection(other))
        error("putAll: Not an array or collection");
      other = other.toArray();
    }
    var n = length(other);
    for (var i = 0; i < n; i++)
      put(other[i]);
  }

  final createWith(other) {
    var r = Queue();
    r.putAll(other);
    return r;
  }
  
  return this;
}

final isQueue(s) {
  return isEnv(s) && hasName("type", s) && s.type == "Queue";
}

final Set() {
  final type = "Set";
  
  var elements = {};

  final isEmpty() {
    return size() == 0;
  }

  final removeAll() {
    elements = {};
  }

  final put(e) {
    if (!contains(e))
      elements[e] = e;
  }

  final remove(e) {
    removeName("" + e, elements);
  }

  final contains(e) {
    return hasName("" + e, elements);
  }

  final size() {
    return length(getNames(elements));
  }

  final toArray() {
    return getValues(elements);
  }

  final putAll(other) {
    if (!isArray(other)) {
      if (!isCollection(other))
        error("putAll: Not an array or collection");
      other = other.toArray();
    }
    var n = length(other);
    for (var i = 0; i < n; i++)
      put(other[i]);
  }

  final createWith(other) {
    var r = Set();
    r.putAll(other);
    return r;
  }

  return this;
}

final isSet(s) {
  return isEnv(s) && hasName("type", s) && s.type == "Set";
}

final isCollection(s) {
  return isQueue(s) || isSet(s) || isStack(s);
}

final toArray(a) {
  if (!isArray(a)) {
    if (!isCollection(a))
      error("toArray: Not an array or collection");
    a = a.toArray();
  }
  return a;
}

final toStack(a) {
  var s = Stack();
  s.putAll(a);
  return s;
}

final toQueue(a) {
  var q = Queue();
  q.putAll(a);
  return q;
}

final toSet(a) {
  var s = Set();
  s.putAll(a);
  return s;
}

final size(a) {
    if (isArray(a))
      return length(a);
    if (!isCollection(a))
      error("size: Not an array or collection");
    return a.size();
}

final contains(a, element) {
  if (isArray(a)) {
    var n = length(a);
    for (var i = 0; i < n; i++)
      if (a[i] == element)
        return true;
    return false;
  }
  if (!isCollection(a))
    error("contains: Not an array or collection");
  return a.contains(element);
}

final map(func, c) {
  var tmp = toArray(c);
  var n = length(tmp);
  var tmp2 = new array[n];
  for (var i = 0; i < n; i++)
    tmp2[i] = func(tmp[i]);
  if (isArray(c))
    return tmp2;
  return c.createWith(tmp2);
}

final filter(pred, c) {
  var tmp = toArray(c);
  var n = length(tmp);
  var tmp2 = Queue();
  for (var i = 0; i < n; i++)
    if (pred(tmp[i]))
      tmp2.put(tmp[i]);
  if (isArray(c))
    return tmp2.toArray();
  return c.createWith(tmp2);
}

final reduce(op, c, init) {
  var tmp = toArray(c);
  var n = length(tmp);
  for (var i = 0; i < n; i++)
    init = op(init, tmp[i]);
  return init;
}

final reducef(op, c) {
  var tmp = toArray(c);
  var n = length(tmp);
  if (n == 0)
    error("reducef: undefined for zero elements");
  var init = tmp[0];
  for (var i = 1; i < n; i++)
    init = op(init, tmp[i]);
  return init;
}

final sum(c) {
  return reduce(lambda(a, b){return a + b;}, c, 0);
}

final prod(c) {
  return reduce(lambda(a, b){return a * b;}, c, 1);
}

final max(c) {
  return reducef(lambda(a, b) {return a > b ? a : b;}, c);
}

final min(c) {
  return reducef(lambda(a, b) {return a < b ? a : b;}, c);
}

final sortQueue(l, comp) {
  if (l.size() == 0)
    return l;
  var f = l.get();
  var sorted = Queue();
  sorted.putAll(sortQueue(filter(lambda(x){ return comp(x, f);},l), comp));
  sorted.put(f);
  sorted.putAll(sortQueue(filter(lambda(x){ return !comp(x, f);}, l), comp));
  return sorted;
}

final sortc(c, comp) {
  var l = Queue();
  l.putAll(c);
  l = sortQueue(l, comp);
  if (isArray(c))
    return l.toArray();
  if (isQueue(c) || isSet(c))
    return l;
  return c.createWith(l);
}

final sort(c) {
  return sortc(c, lambda(x, f) { return x < f; });
}

final append(a, b) {
  var tmp;
  if (isArray(a))
    (tmp = Queue()).putAll(a);
  else if (isCollection(a))
    tmp = a.createWith(a);
  else
    error("append: Is not an array or a collection");
  tmp.putAll(b);
  if (isArray(a))
    return tmp.toArray();
  return tmp;
}

final mappend(func, a) {
  return reduce(append, map(func, a), []);
}

final chooseOne(c) {
  var tmp = toArray(c);
  return tmp[random(length(tmp))];
}

final range(lower, upper) {
  var n = upper - lower + 1;
  var result = new array[n];
  for (var i = 0; i < n; i++)
    result[i] = lower + i;
  return result;
}
