import warnings
import matplotlib.pyplot as plt
import numpy as np
warnings.filterwarnings("ignore")

x=np.linspace(0,2*np.pi, 50)
plt.plot(x, np.sin(x))
plt.show()