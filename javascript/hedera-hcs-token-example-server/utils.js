exports.toHexString = function  (byteArray) {
  const chars = new Uint8Array(byteArray.length * 2)
  const alpha = 'a'.charCodeAt(0) - 10
  const digit = '0'.charCodeAt(0)

  let p = 0
  for (let i = 0; i < byteArray.length; i++) {
    let nibble = byteArray[i] >>> 4
    chars[p++] = nibble > 9 ? nibble + alpha : nibble + digit
    nibble = byteArray[i] & 0xF
    chars[p++] = nibble > 9 ? nibble + alpha : nibble + digit
  }
  return String.fromCharCode.apply(null, chars)
}

exports.secondsToDate = function (time) {
  const date = new Date(1970, 0, 1)
  date.setSeconds(time.seconds)
  return date
}
