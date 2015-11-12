#!/usr/bin/env python

import argparse
import numpy as np
import sys
from matplotlib import pyplot as plt


def main(argv):
    meas = np.genfromtxt(argv[0], delimiter=',', dtype=float, skip_header=1)
    v = meas[:, 0]
    t = meas[:, 1]

    p_v2t = np.polyfit(v, t, 1)
    f_v2t = np.poly1d(p_v2t)
    print p_v2t

    p_t2v = np.polyfit(t, v, 1)
    f_t2v = np.poly1d(p_t2v)

    print p_t2v

    fig = plt.figure()
    sp = fig.add_subplot(111)
    sp.set_xlabel("Value")
    sp.set_ylabel("Temperature")
    sp.plot(v, t, 'b.')

    x = np.linspace(min(v), max(v), 10)
    sp.plot(x, f_v2t(x), 'g-')
    sp.plot(x, (1/128.0) * x, 'r-')
    plt.show()


if __name__ == '__main__':
    main(sys.argv[1:])
