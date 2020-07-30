package com.wittsfamily.approximations.generator;

import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;
import org.apfloat.ApfloatRuntimeException;
import org.apfloat.OverflowException;

public class ApfloatWithInf {
	public static final Apfloat INF = new Apfloat("1e500000", 50);
	public static final Apfloat NEGATIVE_INF = new Apfloat("-1e500000", 50);
	public static final Apfloat NaN = new Apfloat("1e500000000", 50);
	public static final Apfloat ZERO = new Apfloat("0", 50);
	public static final Apfloat ONE = new Apfloat("1", 50);

	public static Apfloat add(Apfloat a, Apfloat b) {
		if (a == NaN || b == NaN) {
			return NaN;
		} else if (a == INF) {
			if (b == NEGATIVE_INF) {
				return NaN;
			} else {
				return INF;
			}
		} else if (b == INF) {
			if (a == NEGATIVE_INF) {
				return NaN;
			} else {
				return INF;
			}
		} else if (a == NEGATIVE_INF || b == NEGATIVE_INF) {
			return NEGATIVE_INF;
		}
		return a.add(b);
	}

	public static Apfloat subtract(Apfloat a, Apfloat b) {
		if (a == NaN || b == NaN) {
			return NaN;
		} else if (a == INF) {
			if (b == INF) {
				return NaN;
			} else {
				return INF;
			}
		} else if (b == NEGATIVE_INF) {
			if (a == NEGATIVE_INF) {
				return NaN;
			} else {
				return INF;
			}
		} else if (a == NEGATIVE_INF || b == INF) {
			return NEGATIVE_INF;
		}
		return a.subtract(b);
	}

	public static Apfloat multiply(Apfloat a, Apfloat b) {
		if (a == NaN || b == NaN) {
			return NaN;
		} else if (a == INF || a == NEGATIVE_INF) {
			int sigB = b.signum();
			if (sigB == 0) {
				return NaN;
			} else if (sigB > 0 ^ a == NEGATIVE_INF) {
				return INF;
			} else {
				return NEGATIVE_INF;
			}
		} else if (b == INF || b == NEGATIVE_INF) {
			int sigA = a.signum();
			if (sigA == 0) {
				return NaN;
			} else if (sigA > 0 ^ b == NEGATIVE_INF) {
				return INF;
			} else {
				return NEGATIVE_INF;
			}
		}
		return a.multiply(b);
	}

	public static Apfloat divide(Apfloat a, Apfloat b) {
		if (a == NaN || b == NaN) {
			return NaN;
		} else if (a == INF || a == NEGATIVE_INF) {
			int sigB = b.signum();
			if (b == INF || b == NEGATIVE_INF) {
				return NaN;
			} else if (sigB > 0 ^ a == NEGATIVE_INF) {
				return INF;
			} else {
				return NEGATIVE_INF;
			}
		} else if (b.signum() == 0) {
			int sigA = a.signum();
			if (sigA == 0) {
				return NaN;
			} else if (sigA > 0) {
				return INF;
			} else {
				return NEGATIVE_INF;
			}
		} else if (b == INF || b == NEGATIVE_INF) {
			return ZERO;
		}
		return a.divide(b);
	}

	public static Apfloat pow(Apfloat a, Apfloat b) {
		int bSig = b.signum();
		if (bSig == 0) {
			return ONE;
		} else if (b.isInteger() && b.intValue() == 1) {
			return a;
		} else if (a == NaN || b == NaN) {
			return NaN;
		} else if (a == NEGATIVE_INF) {
			if (!b.isInteger() || b == INF || b == NEGATIVE_INF) {
				return NaN;
			} else if (bSig < 0) {
				return ZERO;
			} else {
				if (b.intValue() % 2 == 0) {
					return INF;
				} else {
					return NEGATIVE_INF;
				}
			}
		} else if (a == INF) {
			if (bSig < 0) {
				return INF;
			} else {
				return ZERO;
			}
		} else if (b.scale() > 99) {
			return NaN;
		}
		try {
			int aSig = a.signum();
			if (aSig < 0) {
				if (!b.isInteger() || b == INF || b == NEGATIVE_INF) {
					return NaN;
				}
				return ApfloatMath.pow(a, b.intValueExact());
			} else if (aSig == 0) {
				if (bSig < 0) {
					return INF;
				} else {
					return ZERO;
				}
			} else if (aSig > 0 && (b == INF || b == NEGATIVE_INF)) {
				int aComp = a.compareTo(ONE);
				if (aComp == 0) {
					return NaN;
				} else if (aComp > 0) {
					if (b == INF) {
						return INF;
					} else {
						return ZERO;
					}
				} else {
					if (b == INF) {
						return ZERO;
					} else {
						return INF;
					}
				}
			}
			return ApfloatMath.pow(a, b);
		} catch (OverflowException e) {
			if (a.signum() < 0) {
				if (b.intValue() % 2 == 0) {
					return INF;
				} else {
					return NEGATIVE_INF;
				}
			} else {
				return INF;
			}
		}
	}

	public static Apfloat exp(Apfloat a) {
		if (a == NaN) {
			return NaN;
		} else if (a == INF) {
			return INF;
		} else if (a == NEGATIVE_INF) {
			return ZERO;
		} else if (a.signum() == 0) {
			return ONE;
		}
		try {
			return ApfloatMath.exp(a);
		} catch (ApfloatRuntimeException ex) {
			return NaN;
		}
	}

	public static Apfloat pow(Apfloat a, int b) {
		if (b == 0) {
			return ONE;
		} else if (b == 1) {
			return a;
		} else if (a == NaN) {
			return NaN;
		} else if (a == NEGATIVE_INF) {
			if (b < 0) {
				return ZERO;
			} else {
				if (b % 2 == 0) {
					return INF;
				} else {
					return NEGATIVE_INF;
				}
			}
		} else if (a == INF) {
			if (b < 0) {
				return INF;
			} else {
				return ZERO;
			}
		}
		if (a.signum() == 0) {
			if (b < 0) {
				return INF;
			} else {
				return ZERO;
			}
		}
		try {
			return ApfloatMath.pow(a, b);
		} catch (ApfloatRuntimeException ex) {
			return NaN;
		}
	}

	public static Apfloat log(Apfloat base, Apfloat a) {// log_(base) of (a)... opposite to the library, but that was
														// driving me mad
		if (a == NaN || base == NaN) {
			return NaN;
		}
		int baseSig = base.signum();
		int aSig = a.signum();
		int baseComp = base.compareTo(ONE);
		int aComp = a.compareTo(ONE);
		if (baseSig < 0 && aSig < 0) {
			Apfloat result = log(negate(base), negate(a));
			if (!result.isInteger() || result.intValue() % 2 == 0) {
				return NaN;
			}
			return result;
		} else if (baseSig < 0 || aSig < 0) {
			return NaN;
		} else if (baseSig == 0 && aSig == 0) {
			return ZERO;
		} else if (aSig == 0) {
			if (baseComp == 0) {
				return NaN;
			} else if (baseComp > 0) {
				return NEGATIVE_INF;
			} else {
				return INF;
			}
		} else if (baseSig == 0) {
			return ZERO;
		} else if (aComp == 0) {
			if (baseComp == 0) {
				return ONE;
			} else {
				return ZERO;
			}
		} else if (baseComp == 0) {
			if (aComp < 0) {
				return NEGATIVE_INF;
			} else {
				return INF;
			}
		} else if (a == INF) {
			if (baseComp > 0) {
				return INF;
			} else {
				return NEGATIVE_INF;
			}
		} else if (base == INF) {
			return ZERO;
		}
		return ApfloatMath.log(a, base);
	}

	public static Apfloat ln(Apfloat a) {
		if (a == NaN) {
			return NaN;
		}
		int aSig = a.signum();
		int aComp = a.compareTo(ONE);
		if (aSig < 0) {
			return NaN;
		} else if (aSig == 0) {
			return NEGATIVE_INF;
		} else if (aComp == 0) {
			return ZERO;
		} else if (a == INF) {
			return INF;
		}
		return ApfloatMath.log(a);
	}

	public static Apfloat sin(Apfloat a) {
		if (a == NaN) {
			return NaN;
		} else if (a == INF) {
			return NaN;
		} else if (a == NEGATIVE_INF) {
			return NaN;
		} else if (a.signum() == 0) {
			return ZERO;
		}
		return ApfloatMath.sin(a);
	}

	public static Apfloat cos(Apfloat a) {
		if (a == NaN) {
			return NaN;
		} else if (a == INF) {
			return NaN;
		} else if (a == NEGATIVE_INF) {
			return NaN;
		} else if (a.signum() == 0) {
			return ONE;
		}
		return ApfloatMath.cos(a);
	}

	public static Apfloat tan(Apfloat a) {
		if (a == NaN) {
			return NaN;
		} else if (a == INF) {
			return NaN;
		} else if (a == NEGATIVE_INF) {
			return NaN;
		} else if (a.signum() == 0) {
			return ZERO;
		}
		return ApfloatMath.tan(a);
	}

	public static Apfloat asin(Apfloat a) {
		if (ApfloatMath.abs(a).compareTo(ONE) > 0) {
			return NaN;
		} else if (a.signum() == 0) {
			return ZERO;
		}
		return ApfloatMath.asin(a);
	}

	public static Apfloat acos(Apfloat a) {
		if (ApfloatMath.abs(a).compareTo(ONE) > 0) {
			return NaN;
		} else if (a.signum() == 0) {
			return ApfloatMath.pi(50).divide(new Apfloat(2, 50));
		}
		return ApfloatMath.acos(a);
	}

	public static Apfloat atan(Apfloat a) {
		if (a == NaN) {
			return NaN;
		} else if (a == INF) {
			return ApfloatMath.pi(50).divide(new Apfloat(2, 50));
		} else if (a == NEGATIVE_INF) {
			return ApfloatMath.pi(50).divide(new Apfloat(2, 50)).negate();
		} else if (a.signum() == 0) {
			return ZERO;
		}
		return ApfloatMath.atan(a);
	}

	public static Apfloat sinh(Apfloat a) {
		if (a == NaN) {
			return NaN;
		} else if (a == INF) {
			return INF;
		} else if (a == NEGATIVE_INF) {
			return NEGATIVE_INF;
		} else if (a.signum() == 0) {
			return ZERO;
		}
		return ApfloatMath.sinh(a);
	}

	public static Apfloat cosh(Apfloat a) {
		if (a == NaN) {
			return NaN;
		} else if (a == INF) {
			return INF;
		} else if (a == NEGATIVE_INF) {
			return INF;
		} else if (a.signum() == 0) {
			return ONE;
		}
		return ApfloatMath.cosh(a);
	}

	public static Apfloat tanh(Apfloat a) {
		if (a == NaN) {
			return NaN;
		} else if (a == INF) {
			return ONE;
		} else if (a == NEGATIVE_INF) {
			return ONE.negate();
		} else if (a.signum() == 0) {
			return ZERO;
		}
		return ApfloatMath.tanh(a);
	}

	public static Apfloat asinh(Apfloat a) {
		if (a == NaN) {
			return NaN;
		} else if (a == INF) {
			return INF;
		} else if (a == NEGATIVE_INF) {
			return NEGATIVE_INF;
		} else if (a.signum() == 0) {
			return ZERO;
		}
		return ApfloatMath.asinh(a);
	}

	public static Apfloat acosh(Apfloat a) {
		if (a == NaN) {
			return NaN;
		} else if (a == INF) {
			return INF;
		} else if (a.compareTo(ONE) < 0) {
			return NaN;
		}
		return ApfloatMath.acosh(a);
	}

	public static Apfloat atanh(Apfloat a) {
		if (ApfloatMath.abs(a).compareTo(ONE) > 0) {
			return NaN;
		} else if (a.signum() == 0) {
			return ZERO;
		}
		return ApfloatMath.atanh(a);
	}

	public static Apfloat negate(Apfloat a) {
		if (a == NaN) {
			return NaN;
		} else if (a == INF) {
			return NEGATIVE_INF;
		} else if (a == NEGATIVE_INF) {
			return INF;
		}
		return a.negate();
	}

	public static Apfloat invert(Apfloat a) {
		if (a == NaN) {
			return NaN;
		} else if (a == INF || a == NEGATIVE_INF) {
			return ZERO;
		} else if (a.signum() == 0) {
			return INF;
		}
		return ONE.divide(a);
	}

	public static Apfloat abs(Apfloat a) {
		if (a == NaN) {
			return NaN;
		} else if (a == INF || a == NEGATIVE_INF) {
			return INF;
		}
		return ApfloatMath.abs(a);
	}

	public static Apfloat sqrt(Apfloat a) {
		if (a == NaN) {
			return NaN;
		} else if (a == INF) {
			return INF;
		} else if (a.signum() < 0) {
			return NaN;
		}
		return ApfloatMath.sqrt(a);
	}
}
