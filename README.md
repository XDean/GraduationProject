# GraduationProject
My Graduation Project about high frequency trading.
## Entrance ##
1. xdean.graduation.workspace.HFT<br>
   
2. xdean.graduation.workspace.EMH<br>
   To verify the efficiency of high-frequency market's by Runs test.
3. xdean.graduation.workspace.Spread<br>
   To calculate the average spread.

## Packages ##
- handler
	- param
		1. ParamSelector. It determine how to select the best param. Now I provide `NatureSelector` and `ConvolutionSelector`.
		2. ParamHandler. It determine how to generate the origin param set and how to handle them. There are `ParamSupplier` and `ParamAdapter`.
	- trader<br>
		1. `Trader` define how to handle the high-frequency data and do trading.
		2. `TraderUtil` provide some common trading strategy like "cut loss", "save earning".
	- some rx operator<br> use `TimeOperator` and `VolumeOperator` to make the order flow to equally spaced interval of time or volume.
- index
	1. `Index` is a statistical progress or a statistics.
	2. You can find most of common statistics in `Indexs` class.
	3. I provide two Financial Technical Indicator, `KDJ` and `MACD`.
	4. Other useful index: `RepoAnalyser`, `MaxDrawdown`, `sharpRatio`.
- io<br> `DataReader` and `DataWriter`. I provide a `CsvSaver` to save the output. Classes in `TianRuan` and `JuBoHua` are handle their company's [data](#data). You can implement new `DataReader` to adapt your data.
- model
- workspace<br> main classes and configs(`Context`)

## <a id="data"/>Data ##
Because of the copyright of the high-frequency data, I can't upload it. If you need some data, you can contact me.