package com.zypex.piflow.profile

import com.zypex.piflow.math.Vector


class CompoundProfileSegment : ProfileSegment() {
    var segments: MutableList<ProfileSegment> = ArrayList()
    override fun upperBound(): Double {
        var highest = segments[0].lowerBound()
        for (i in 1 until segments.size) {
            val current = segments[i].lowerBound()
            if (current > highest) highest = current
        }
        return highest
    }

    override fun lowerBound(): Double {
        var lowest = segments[0].lowerBound()
        for (i in 1 until segments.size) {
            val current = segments[i].lowerBound()
            if (current < lowest) lowest = current
        }
        return lowest
    }

    override val length: Double
        get() {
            var total = 0.0
            for (s in segments) total += s.length
            return total
        }

    @Throws(ClassCastException::class)
    override fun offset(offset: Double): CompoundProfileSegment {
        val out = CompoundProfileSegment()
        for (segment in segments) {
            out.add(segment.offset(offset))
        }
        return out
    }

    fun add(segment: ProfileSegment) {
        if (segment is CompoundProfileSegment) {
            for (sub in segment.segments) add(sub)
        }
        segments.add(segment)
    }

    fun append(segment: ProfileSegment) {
        if (segment is CompoundProfileSegment) {
            for (sub in segment.segments) append(sub)
            return
        }
        add(segment.offset(upperBound()))
    }

    override fun getT(pos: Vector): Double {
        var t = segments[0].getT(pos)
        var closest = segments[0](t).position
        for (i in 1 until segments.size) {
            val segment = segments[i]
            val currentT = segment.getT(pos)
            val currentPos = segment(currentT).position
            if (currentPos.subtract(pos).magnitude < closest.subtract(pos).magnitude) {
                t = currentT
                closest = currentPos
            }
        }
        return t
    }

    override fun invoke(input: Double): Derivatives<Vector> = TODO()
}