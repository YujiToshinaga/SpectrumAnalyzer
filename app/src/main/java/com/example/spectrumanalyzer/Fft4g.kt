package com.example.spectrumanalyzer

import kotlin.math.*

class Fft4g internal constructor(private val n: Int) {
    private val ip: IntArray
    private val w: DoubleArray

    init {
        ip = IntArray(2 + Math.sqrt(n.toDouble() / 2.0).toInt() + 1)
        w = DoubleArray(n / 2)
        ip[0] = 0
    }

    fun rdft(isgn: Int, a: DoubleArray) {
        var nw: Int
        var nc: Int
        val xi: Double
        nw = ip[0]
        if (n > nw shl 2) {
            nw = n shr 2
            makewt(nw)
        }
        nc = ip[1]
        if (n > nc shl 2) {
            nc = n shr 2
            makect(nc, w, nw)
        }
        if (isgn >= 0) {
            if (n > 4) {
                bitrv2(n, a)
                cftfsub(a)
                rftfsub(a, nc, w, nw)
            } else if (n == 4) {
                cftfsub(a)
            }
            xi = a[0] - a[1]
            a[0] += a[1]
            a[1] = xi
        } else {
            a[1] = 0.5 * (a[0] - a[1])
            a[0] -= a[1]
            if (n > 4) {
                rftbsub(a, nc, w, nw)
                bitrv2(n, a)
                cftbsub(a)
            } else if (n == 4) {
                cftfsub(a)
            }
        }
    }

    private fun makewt(nw: Int) {
        var j: Int
        val nwh: Int
        val delta: Double
        var x: Double
        var y: Double
        ip[0] = nw
        ip[1] = 1
        if (nw > 2) {
            nwh = nw shr 1
            delta = atan(1.0) / nwh
            w[0] = 1.0
            w[1] = 0.0
            w[nwh] = cos(delta * nwh)
            w[nwh + 1] = w[nwh]
            if (nwh > 2) {
                j = 2
                while (j < nwh) {
                    x = cos(delta * j)
                    y = sin(delta * j)
                    w[j] = x
                    w[j + 1] = y
                    w[nw - j] = y
                    w[nw - j + 1] = x
                    j += 2
                }
                bitrv2(nw, w)
            }
        }
    }

    fun makect(nc: Int, c: DoubleArray, nw: Int) {
        var j: Int
        val nch: Int
        val delta: Double
        ip[1] = nc
        if (nc > 1) {
            nch = nc shr 1
            delta = atan(1.0) / nch
            c[nw + 0] = cos(delta * nch)
            c[nw + nch] = 0.5 * c[nw + 0]
            j = 1
            while (j < nch) {
                c[nw + j] = 0.5 * cos(delta * j)
                c[nw + nc - j] = 0.5 * sin(delta * j)
                j++
            }
        }
    }

    /* -------- child routines -------- */
    private fun bitrv2(n: Int, a: DoubleArray) {
        var j: Int
        var j1: Int
        var k: Int
        var k1: Int
        var l: Int
        var m: Int
        val m2: Int
        var xr: Double
        var xi: Double
        var yr: Double
        var yi: Double
        ip[2 + 0] = 0
        l = n
        m = 1
        while (m shl 3 < l) {
            l = l shr 1
            j = 0
            while (j < m) {
                ip[2 + m + j] = ip[2 + j] + l
                j++
            }
            m = m shl 1
        }
        m2 = 2 * m
        if (m shl 3 == l) {
            k = 0
            while (k < m) {
                j = 0
                while (j < k) {
                    j1 = 2 * j + ip[2 + k]
                    k1 = 2 * k + ip[2 + j]
                    xr = a[j1]
                    xi = a[j1 + 1]
                    yr = a[k1]
                    yi = a[k1 + 1]
                    a[j1] = yr
                    a[j1 + 1] = yi
                    a[k1] = xr
                    a[k1 + 1] = xi
                    j1 += m2
                    k1 += 2 * m2
                    xr = a[j1]
                    xi = a[j1 + 1]
                    yr = a[k1]
                    yi = a[k1 + 1]
                    a[j1] = yr
                    a[j1 + 1] = yi
                    a[k1] = xr
                    a[k1 + 1] = xi
                    j1 += m2
                    k1 -= m2
                    xr = a[j1]
                    xi = a[j1 + 1]
                    yr = a[k1]
                    yi = a[k1 + 1]
                    a[j1] = yr
                    a[j1 + 1] = yi
                    a[k1] = xr
                    a[k1 + 1] = xi
                    j1 += m2
                    k1 += 2 * m2
                    xr = a[j1]
                    xi = a[j1 + 1]
                    yr = a[k1]
                    yi = a[k1 + 1]
                    a[j1] = yr
                    a[j1 + 1] = yi
                    a[k1] = xr
                    a[k1 + 1] = xi
                    j++
                }
                j1 = 2 * k + m2 + ip[2 + k]
                k1 = j1 + m2
                xr = a[j1]
                xi = a[j1 + 1]
                yr = a[k1]
                yi = a[k1 + 1]
                a[j1] = yr
                a[j1 + 1] = yi
                a[k1] = xr
                a[k1 + 1] = xi
                k++
            }
        } else {
            k = 1
            while (k < m) {
                j = 0
                while (j < k) {
                    j1 = 2 * j + ip[2 + k]
                    k1 = 2 * k + ip[2 + j]
                    xr = a[j1]
                    xi = a[j1 + 1]
                    yr = a[k1]
                    yi = a[k1 + 1]
                    a[j1] = yr
                    a[j1 + 1] = yi
                    a[k1] = xr
                    a[k1 + 1] = xi
                    j1 += m2
                    k1 += m2
                    xr = a[j1]
                    xi = a[j1 + 1]
                    yr = a[k1]
                    yi = a[k1 + 1]
                    a[j1] = yr
                    a[j1 + 1] = yi
                    a[k1] = xr
                    a[k1 + 1] = xi
                    j++
                }
                k++
            }
        }
    }

    private fun rftfsub(a: DoubleArray, nc: Int, c: DoubleArray, nw: Int) {
        var j: Int
        var k: Int
        var kk: Int
        val ks: Int
        val m: Int
        var wkr: Double
        var wki: Double
        var xr: Double
        var xi: Double
        var yr: Double
        var yi: Double
        m = n shr 1
        ks = 2 * nc / m
        kk = 0
        j = 2
        while (j < m) {
            k = n - j
            kk += ks
            wkr = 0.5 - c[nw + nc - kk]
            wki = c[nw + kk]
            xr = a[j] - a[k]
            xi = a[j + 1] + a[k + 1]
            yr = wkr * xr - wki * xi
            yi = wkr * xi + wki * xr
            a[j] -= yr
            a[j + 1] -= yi
            a[k] += yr
            a[k + 1] -= yi
            j += 2
        }
    }

    private fun rftbsub(a: DoubleArray, nc: Int, c: DoubleArray, nw: Int) {
        var j: Int
        var k: Int
        var kk: Int
        val ks: Int
        val m: Int
        var wkr: Double
        var wki: Double
        var xr: Double
        var xi: Double
        var yr: Double
        var yi: Double
        a[1] = -a[1]
        m = n shr 1
        ks = 2 * nc / m
        kk = 0
        j = 2
        while (j < m) {
            k = n - j
            kk += ks
            wkr = 0.5 - c[nw + nc - kk]
            wki = c[nw + kk]
            xr = a[j] - a[k]
            xi = a[j + 1] + a[k + 1]
            yr = wkr * xr + wki * xi
            yi = wkr * xi - wki * xr
            a[j] -= yr
            a[j + 1] = yi - a[j + 1]
            a[k] += yr
            a[k + 1] = yi - a[k + 1]
            j += 2
        }
        a[m + 1] = -a[m + 1]
    }

    private fun cftfsub(a: DoubleArray) {
        var j: Int
        var j1: Int
        var j2: Int
        var j3: Int
        var l: Int
        var x0r: Double
        var x0i: Double
        var x1r: Double
        var x1i: Double
        var x2r: Double
        var x2i: Double
        var x3r: Double
        var x3i: Double
        l = 2
        if (n > 8) {
            cft1st(a)
            l = 8
            while (l shl 2 < n) {
                cftmdl(l, a)
                l = l shl 2
            }
        }
        if (l shl 2 == n) {
            j = 0
            while (j < l) {
                j1 = j + l
                j2 = j1 + l
                j3 = j2 + l
                x0r = a[j] + a[j1]
                x0i = a[j + 1] + a[j1 + 1]
                x1r = a[j] - a[j1]
                x1i = a[j + 1] - a[j1 + 1]
                x2r = a[j2] + a[j3]
                x2i = a[j2 + 1] + a[j3 + 1]
                x3r = a[j2] - a[j3]
                x3i = a[j2 + 1] - a[j3 + 1]
                a[j] = x0r + x2r
                a[j + 1] = x0i + x2i
                a[j2] = x0r - x2r
                a[j2 + 1] = x0i - x2i
                a[j1] = x1r - x3i
                a[j1 + 1] = x1i + x3r
                a[j3] = x1r + x3i
                a[j3 + 1] = x1i - x3r
                j += 2
            }
        } else {
            j = 0
            while (j < l) {
                j1 = j + l
                x0r = a[j] - a[j1]
                x0i = a[j + 1] - a[j1 + 1]
                a[j] += a[j1]
                a[j + 1] += a[j1 + 1]
                a[j1] = x0r
                a[j1 + 1] = x0i
                j += 2
            }
        }
    }

    private fun cftbsub(a: DoubleArray) {
        var j: Int
        var j1: Int
        var j2: Int
        var j3: Int
        var l: Int
        var x0r: Double
        var x0i: Double
        var x1r: Double
        var x1i: Double
        var x2r: Double
        var x2i: Double
        var x3r: Double
        var x3i: Double
        l = 2
        if (n > 8) {
            cft1st(a)
            l = 8
            while (l shl 2 < n) {
                cftmdl(l, a)
                l = l shl 2
            }
        }
        if (l shl 2 == n) {
            j = 0
            while (j < l) {
                j1 = j + l
                j2 = j1 + l
                j3 = j2 + l
                x0r = a[j] + a[j1]
                x0i = -a[j + 1] - a[j1 + 1]
                x1r = a[j] - a[j1]
                x1i = -a[j + 1] + a[j1 + 1]
                x2r = a[j2] + a[j3]
                x2i = a[j2 + 1] + a[j3 + 1]
                x3r = a[j2] - a[j3]
                x3i = a[j2 + 1] - a[j3 + 1]
                a[j] = x0r + x2r
                a[j + 1] = x0i - x2i
                a[j2] = x0r - x2r
                a[j2 + 1] = x0i + x2i
                a[j1] = x1r - x3i
                a[j1 + 1] = x1i - x3r
                a[j3] = x1r + x3i
                a[j3 + 1] = x1i + x3r
                j += 2
            }
        } else {
            j = 0
            while (j < l) {
                j1 = j + l
                x0r = a[j] - a[j1]
                x0i = -a[j + 1] + a[j1 + 1]
                a[j] += a[j1]
                a[j + 1] = -a[j + 1] - a[j1 + 1]
                a[j1] = x0r
                a[j1 + 1] = x0i
                j += 2
            }
        }
    }

    private fun cft1st(a: DoubleArray) {
        var j: Int
        var k1: Int
        var k2: Int
        var wk1r: Double
        var wk1i: Double
        var wk2r: Double
        var wk2i: Double
        var wk3r: Double
        var wk3i: Double
        var x0r: Double
        var x0i: Double
        var x1r: Double
        var x1i: Double
        var x2r: Double
        var x2i: Double
        var x3r: Double
        var x3i: Double
        x0r = a[0] + a[2]
        x0i = a[1] + a[3]
        x1r = a[0] - a[2]
        x1i = a[1] - a[3]
        x2r = a[4] + a[6]
        x2i = a[5] + a[7]
        x3r = a[4] - a[6]
        x3i = a[5] - a[7]
        a[0] = x0r + x2r
        a[1] = x0i + x2i
        a[4] = x0r - x2r
        a[5] = x0i - x2i
        a[2] = x1r - x3i
        a[3] = x1i + x3r
        a[6] = x1r + x3i
        a[7] = x1i - x3r
        wk1r = w[2]
        x0r = a[8] + a[10]
        x0i = a[9] + a[11]
        x1r = a[8] - a[10]
        x1i = a[9] - a[11]
        x2r = a[12] + a[14]
        x2i = a[13] + a[15]
        x3r = a[12] - a[14]
        x3i = a[13] - a[15]
        a[8] = x0r + x2r
        a[9] = x0i + x2i
        a[12] = x2i - x0i
        a[13] = x0r - x2r
        x0r = x1r - x3i
        x0i = x1i + x3r
        a[10] = wk1r * (x0r - x0i)
        a[11] = wk1r * (x0r + x0i)
        x0r = x3i + x1r
        x0i = x3r - x1i
        a[14] = wk1r * (x0i - x0r)
        a[15] = wk1r * (x0i + x0r)
        k1 = 0
        j = 16
        while (j < n) {
            k1 += 2
            k2 = 2 * k1
            wk2r = w[k1]
            wk2i = w[k1 + 1]
            wk1r = w[k2]
            wk1i = w[k2 + 1]
            wk3r = wk1r - 2 * wk2i * wk1i
            wk3i = 2 * wk2i * wk1r - wk1i
            x0r = a[j] + a[j + 2]
            x0i = a[j + 1] + a[j + 3]
            x1r = a[j] - a[j + 2]
            x1i = a[j + 1] - a[j + 3]
            x2r = a[j + 4] + a[j + 6]
            x2i = a[j + 5] + a[j + 7]
            x3r = a[j + 4] - a[j + 6]
            x3i = a[j + 5] - a[j + 7]
            a[j] = x0r + x2r
            a[j + 1] = x0i + x2i
            x0r -= x2r
            x0i -= x2i
            a[j + 4] = wk2r * x0r - wk2i * x0i
            a[j + 5] = wk2r * x0i + wk2i * x0r
            x0r = x1r - x3i
            x0i = x1i + x3r
            a[j + 2] = wk1r * x0r - wk1i * x0i
            a[j + 3] = wk1r * x0i + wk1i * x0r
            x0r = x1r + x3i
            x0i = x1i - x3r
            a[j + 6] = wk3r * x0r - wk3i * x0i
            a[j + 7] = wk3r * x0i + wk3i * x0r
            wk1r = w[k2 + 2]
            wk1i = w[k2 + 3]
            wk3r = wk1r - 2 * wk2r * wk1i
            wk3i = 2 * wk2r * wk1r - wk1i
            x0r = a[j + 8] + a[j + 10]
            x0i = a[j + 9] + a[j + 11]
            x1r = a[j + 8] - a[j + 10]
            x1i = a[j + 9] - a[j + 11]
            x2r = a[j + 12] + a[j + 14]
            x2i = a[j + 13] + a[j + 15]
            x3r = a[j + 12] - a[j + 14]
            x3i = a[j + 13] - a[j + 15]
            a[j + 8] = x0r + x2r
            a[j + 9] = x0i + x2i
            x0r -= x2r
            x0i -= x2i
            a[j + 12] = -wk2i * x0r - wk2r * x0i
            a[j + 13] = -wk2i * x0i + wk2r * x0r
            x0r = x1r - x3i
            x0i = x1i + x3r
            a[j + 10] = wk1r * x0r - wk1i * x0i
            a[j + 11] = wk1r * x0i + wk1i * x0r
            x0r = x1r + x3i
            x0i = x1i - x3r
            a[j + 14] = wk3r * x0r - wk3i * x0i
            a[j + 15] = wk3r * x0i + wk3i * x0r
            j += 16
        }
    }

    private fun cftmdl(l: Int, a: DoubleArray) {
        var j: Int
        var j1: Int
        var j2: Int
        var j3: Int
        var k: Int
        var k1: Int
        var k2: Int
        val m: Int
        val m2: Int
        var wk1r: Double
        var wk1i: Double
        var wk2r: Double
        var wk2i: Double
        var wk3r: Double
        var wk3i: Double
        var x0r: Double
        var x0i: Double
        var x1r: Double
        var x1i: Double
        var x2r: Double
        var x2i: Double
        var x3r: Double
        var x3i: Double
        m = l shl 2
        j = 0
        while (j < l) {
            j1 = j + l
            j2 = j1 + l
            j3 = j2 + l
            x0r = a[j] + a[j1]
            x0i = a[j + 1] + a[j1 + 1]
            x1r = a[j] - a[j1]
            x1i = a[j + 1] - a[j1 + 1]
            x2r = a[j2] + a[j3]
            x2i = a[j2 + 1] + a[j3 + 1]
            x3r = a[j2] - a[j3]
            x3i = a[j2 + 1] - a[j3 + 1]
            a[j] = x0r + x2r
            a[j + 1] = x0i + x2i
            a[j2] = x0r - x2r
            a[j2 + 1] = x0i - x2i
            a[j1] = x1r - x3i
            a[j1 + 1] = x1i + x3r
            a[j3] = x1r + x3i
            a[j3 + 1] = x1i - x3r
            j += 2
        }
        wk1r = w[2]
        j = m
        while (j < l + m) {
            j1 = j + l
            j2 = j1 + l
            j3 = j2 + l
            x0r = a[j] + a[j1]
            x0i = a[j + 1] + a[j1 + 1]
            x1r = a[j] - a[j1]
            x1i = a[j + 1] - a[j1 + 1]
            x2r = a[j2] + a[j3]
            x2i = a[j2 + 1] + a[j3 + 1]
            x3r = a[j2] - a[j3]
            x3i = a[j2 + 1] - a[j3 + 1]
            a[j] = x0r + x2r
            a[j + 1] = x0i + x2i
            a[j2] = x2i - x0i
            a[j2 + 1] = x0r - x2r
            x0r = x1r - x3i
            x0i = x1i + x3r
            a[j1] = wk1r * (x0r - x0i)
            a[j1 + 1] = wk1r * (x0r + x0i)
            x0r = x3i + x1r
            x0i = x3r - x1i
            a[j3] = wk1r * (x0i - x0r)
            a[j3 + 1] = wk1r * (x0i + x0r)
            j += 2
        }
        k1 = 0
        m2 = 2 * m
        k = m2
        while (k < n) {
            k1 += 2
            k2 = 2 * k1
            wk2r = w[k1]
            wk2i = w[k1 + 1]
            wk1r = w[k2]
            wk1i = w[k2 + 1]
            wk3r = wk1r - 2 * wk2i * wk1i
            wk3i = 2 * wk2i * wk1r - wk1i
            j = k
            while (j < l + k) {
                j1 = j + l
                j2 = j1 + l
                j3 = j2 + l
                x0r = a[j] + a[j1]
                x0i = a[j + 1] + a[j1 + 1]
                x1r = a[j] - a[j1]
                x1i = a[j + 1] - a[j1 + 1]
                x2r = a[j2] + a[j3]
                x2i = a[j2 + 1] + a[j3 + 1]
                x3r = a[j2] - a[j3]
                x3i = a[j2 + 1] - a[j3 + 1]
                a[j] = x0r + x2r
                a[j + 1] = x0i + x2i
                x0r -= x2r
                x0i -= x2i
                a[j2] = wk2r * x0r - wk2i * x0i
                a[j2 + 1] = wk2r * x0i + wk2i * x0r
                x0r = x1r - x3i
                x0i = x1i + x3r
                a[j1] = wk1r * x0r - wk1i * x0i
                a[j1 + 1] = wk1r * x0i + wk1i * x0r
                x0r = x1r + x3i
                x0i = x1i - x3r
                a[j3] = wk3r * x0r - wk3i * x0i
                a[j3 + 1] = wk3r * x0i + wk3i * x0r
                j += 2
            }
            wk1r = w[k2 + 2]
            wk1i = w[k2 + 3]
            wk3r = wk1r - 2 * wk2r * wk1i
            wk3i = 2 * wk2r * wk1r - wk1i
            j = k + m
            while (j < l + (k + m)) {
                j1 = j + l
                j2 = j1 + l
                j3 = j2 + l
                x0r = a[j] + a[j1]
                x0i = a[j + 1] + a[j1 + 1]
                x1r = a[j] - a[j1]
                x1i = a[j + 1] - a[j1 + 1]
                x2r = a[j2] + a[j3]
                x2i = a[j2 + 1] + a[j3 + 1]
                x3r = a[j2] - a[j3]
                x3i = a[j2 + 1] - a[j3 + 1]
                a[j] = x0r + x2r
                a[j + 1] = x0i + x2i
                x0r -= x2r
                x0i -= x2i
                a[j2] = -wk2i * x0r - wk2r * x0i
                a[j2 + 1] = -wk2i * x0i + wk2r * x0r
                x0r = x1r - x3i
                x0i = x1i + x3r
                a[j1] = wk1r * x0r - wk1i * x0i
                a[j1 + 1] = wk1r * x0i + wk1i * x0r
                x0r = x1r + x3i
                x0i = x1i - x3r
                a[j3] = wk3r * x0r - wk3i * x0i
                a[j3 + 1] = wk3r * x0i + wk3i * x0r
                j += 2
            }
            k += m2
        }
    }
}
