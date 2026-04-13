const STORAGE_KEY = 'share_capture_records'
const PLATFORM_OPTIONS = ['B站', 'YouTube', 'X', '微信公众号', '未标记']
let lastCapturedSignature = ''

function ensureAppPlus() {
  return typeof plus !== 'undefined' && plus.android
}

function safeCall(fn, fallback = null) {
  try {
    const value = fn()
    return value === undefined ? fallback : value
  } catch (error) {
    return fallback
  }
}

function safeToString(value) {
  if (value === null || value === undefined) {
    return ''
  }
  try {
    return String(value)
  } catch (error) {
    return Object.prototype.toString.call(value)
  }
}

function toPlainValue(value) {
  if (value === null || value === undefined) {
    return null
  }
  const jsType = typeof value
  if (jsType === 'string' || jsType === 'number' || jsType === 'boolean') {
    return value
  }
  return {
    stringValue: safeToString(value),
    nativeType: safeCall(() => value.getClass().getName(), null)
  }
}

function bundleToObject(bundle) {
  if (!bundle) {
    return {}
  }
  const result = {}
  const keySet = safeCall(() => bundle.keySet(), null)
  if (!keySet) {
    return result
  }
  const iterator = safeCall(() => keySet.iterator(), null)
  while (iterator && safeCall(() => iterator.hasNext(), false)) {
    const key = safeToString(iterator.next())
    const rawValue = safeCall(() => bundle.get(key), null)
    result[key] = {
      stringValue: safeToString(rawValue),
      nativeType: safeCall(() => rawValue && rawValue.getClass ? rawValue.getClass().getName() : null, null)
    }
  }
  return result
}

function categoriesToArray(categories) {
  if (!categories) {
    return []
  }
  const items = []
  const iterator = safeCall(() => categories.iterator(), null)
  while (iterator && safeCall(() => iterator.hasNext(), false)) {
    items.push(safeToString(iterator.next()))
  }
  return items
}

function clipDataToObject(clipData) {
  if (!clipData) {
    return null
  }
  const itemCount = safeCall(() => clipData.getItemCount(), 0)
  const description = safeCall(() => clipData.getDescription(), null)
  const descriptionMimeTypes = []
  const mimeTypeCount = safeCall(() => description && description.getMimeTypeCount ? description.getMimeTypeCount() : 0, 0)
  for (let index = 0; index < mimeTypeCount; index += 1) {
    descriptionMimeTypes.push(safeToString(safeCall(() => description.getMimeType(index), '')))
  }
  const items = []
  for (let index = 0; index < itemCount; index += 1) {
    const item = safeCall(() => clipData.getItemAt(index), null)
    items.push({
      index,
      text: safeToString(safeCall(() => item && item.getText ? item.getText() : null, null)),
      htmlText: safeToString(safeCall(() => item && item.getHtmlText ? item.getHtmlText() : null, null)),
      uri: safeToString(safeCall(() => item && item.getUri ? item.getUri() : null, null)),
      intent: safeToString(safeCall(() => item && item.getIntent ? item.getIntent() : null, null)),
      raw: safeToString(item)
    })
  }
  return {
    itemCount,
    descriptionLabel: safeToString(safeCall(() => description && description.getLabel ? description.getLabel() : null, null)),
    descriptionMimeTypes,
    items
  }
}

function collectIntentSnapshot(intent) {
  if (!intent) {
    return null
  }
  const extras = safeCall(() => intent.getExtras(), null)
  const clipData = safeCall(() => intent.getClipData(), null)
  return {
    action: safeToString(safeCall(() => intent.getAction(), null)),
    type: safeToString(safeCall(() => intent.getType(), null)),
    scheme: safeToString(safeCall(() => intent.getScheme(), null)),
    dataString: safeToString(safeCall(() => intent.getDataString(), null)),
    packageName: safeToString(safeCall(() => intent.getPackage(), null)),
    component: safeToString(safeCall(() => intent.getComponent(), null)),
    flags: safeCall(() => intent.getFlags(), null),
    categories: categoriesToArray(safeCall(() => intent.getCategories(), null)),
    extraText: safeToString(safeCall(() => intent.getStringExtra('android.intent.extra.TEXT'), null)),
    extraSubject: safeToString(safeCall(() => intent.getStringExtra('android.intent.extra.SUBJECT'), null)),
    extraHtmlText: safeToString(safeCall(() => intent.getStringExtra('android.intent.extra.HTML_TEXT'), null)),
    extraStream: safeToString(safeCall(() => intent.getParcelableExtra('android.intent.extra.STREAM'), null)),
    extraTitle: safeToString(safeCall(() => intent.getStringExtra('android.intent.extra.TITLE'), null)),
    extraReferrerName: safeToString(safeCall(() => intent.getStringExtra('android.intent.extra.REFERRER_NAME'), null)),
    extras: bundleToObject(extras),
    clipData: clipDataToObject(clipData),
    rawIntent: safeToString(intent)
  }
}

function buildSnapshotSignature(snapshot) {
  return JSON.stringify({
    action: snapshot.action || '',
    type: snapshot.type || '',
    dataString: snapshot.dataString || '',
    extraText: snapshot.extraText || '',
    extraSubject: snapshot.extraSubject || '',
    extraHtmlText: snapshot.extraHtmlText || '',
    extraStream: snapshot.extraStream || '',
    extras: snapshot.extras || {},
    clipData: snapshot.clipData || null,
    rawIntent: snapshot.rawIntent || ''
  })
}

function isShareIntentSnapshot(snapshot) {
  if (!snapshot) {
    return false
  }
  const action = snapshot.action || ''
  if (action === 'android.intent.action.SEND' || action === 'android.intent.action.SEND_MULTIPLE') {
    return true
  }
  if (snapshot.extraText || snapshot.extraSubject || snapshot.extraHtmlText || snapshot.extraStream) {
    return true
  }
  if (snapshot.clipData && snapshot.clipData.itemCount > 0) {
    return true
  }
  return Object.keys(snapshot.extras || {}).length > 0
}

function getMainIntent() {
  if (!ensureAppPlus()) {
    return null
  }
  const mainActivity = plus.android.runtimeMainActivity()
  plus.android.importClass(mainActivity)
  return safeCall(() => mainActivity.getIntent(), null)
}

function buildRecord(snapshot, trigger) {
  return {
    id: `${Date.now()}-${Math.random().toString(16).slice(2, 8)}`,
    createdAt: new Date().toISOString(),
    trigger,
    platformLabel: '未标记',
    testerNote: '',
    snapshot
  }
}

function loadRecords() {
  return uni.getStorageSync(STORAGE_KEY) || []
}

function saveRecords(records) {
  uni.setStorageSync(STORAGE_KEY, records)
}

export function getPlatformOptions() {
  return PLATFORM_OPTIONS.slice()
}

export function captureCurrentIntent(trigger = 'manual') {
  if (!ensureAppPlus()) {
    return { ok: false, message: '当前环境不是 App-Android 真机/基座环境' }
  }
  const intent = getMainIntent()
  const snapshot = collectIntentSnapshot(intent)
  if (!snapshot) {
    return { ok: false, message: '没有读取到 Intent' }
  }
  if (!isShareIntentSnapshot(snapshot)) {
    return { ok: false, message: '当前 Intent 不是系统分享数据' }
  }
  const signature = buildSnapshotSignature(snapshot)
  if (signature === lastCapturedSignature) {
    return { ok: false, message: '当前分享数据与上一条相同，已跳过重复采集' }
  }
  const records = loadRecords()
  const record = buildRecord(snapshot, trigger)
  records.unshift(record)
  saveRecords(records)
  lastCapturedSignature = signature
  return { ok: true, record, records }
}

export function listRecords() {
  return loadRecords()
}

export function updateRecordMeta(id, patch) {
  const records = loadRecords()
  const nextRecords = records.map((item) => {
    if (item.id !== id) {
      return item
    }
    return {
      ...item,
      ...patch
    }
  })
  saveRecords(nextRecords)
  return nextRecords
}

export function clearRecords() {
  saveRecords([])
  lastCapturedSignature = ''
  return []
}

export function waitForAppPlusReady(callback, options = {}) {
  const intervalMs = options.intervalMs || 150
  const timeoutMs = options.timeoutMs || 6000
  const startTime = Date.now()
  const timer = setInterval(() => {
    if (ensureAppPlus()) {
      clearInterval(timer)
      callback(true)
      return
    }
    if (Date.now() - startTime >= timeoutMs) {
      clearInterval(timer)
      callback(false)
    }
  }, intervalMs)
  return () => clearInterval(timer)
}

export function startIntentPolling(callback, options = {}) {
  const intervalMs = options.intervalMs || 1500
  if (!ensureAppPlus()) {
    return () => {}
  }
  const timer = setInterval(() => {
    const result = captureCurrentIntent('polling')
    if (result.ok) {
      callback(result)
    }
  }, intervalMs)
  return () => clearInterval(timer)
}
