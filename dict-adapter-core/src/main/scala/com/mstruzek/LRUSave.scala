package com.mstruzek

import java.util.{Map => JMap}

class LRUSave[K,V](maxEntries: Int) extends java.util.LinkedHashMap[K,V](100, .75f, true) {

  override def removeEldestEntry(eldest: JMap.Entry[K, V]): Boolean = size > maxEntries
}
