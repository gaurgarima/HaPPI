# HaPPI
A framework to support provenance-aware query computation over probabilistic knowledge graphs with the capability of providing fine-grained provenance of
the probability computaiton.

## Prerequisite Installation

* Download the knowledge compilation tool C2D compiler using the link (http://reasoning.cs.ucla.edu/c2d/)

## Setup

* Download and unzip data files: gMark answer set and YAGO2 answer set from (tbd) and place them in the project HOME folder
* Run the following commnads to setup the directory structure and compile the code,
```
./setup.sh
./compile.sh
```

## Usage

We measured the performance of HaPPI using two datasets <dataset>, gmark and yago, over multiple runs <run>.
To compute the probability of each answer of a query result set, any one of the three approaches can be employed -- possible world computation (PosWorld),
our proposed symbolic expression computation method (HaPPI) and knowledge compilation (using C2D compiler).


### Probability computation

1. Using both the posWorld and HaPPI methodology together for the probability computation
```
java HappiQueryExecutor <dataset> <run>
```
2. To run C2D compiler to translate a given Boolean formula to a d-DNNF formula and further to evaluate the probability using the compiled form,
```
java TseytinTransformation c2d <dataset> <run>
```
### Probability maintenance under edge insertion operations
```
java UpdateMaintenance <dataset> insertion <run>
```

## HaPPI performance assessment

We measured the time taken to compute the probaiblity via symbolic expression construction and also
the time taken to update each answer of query qID using the incremental maintenance approach of HaPPI. 
For experiemntal purpose, the performance of HaPPI over 10 runs can using the following commands,

```
./happi.sh <dataset> <qId>
./update.sh <dataset> <qId>
```
We can combine the runtime of each query over 10 runs into a single file under the following setups,

1. For each query qId, collate the total probability computation time per answer taken by the Brute-force possible world computation and HaPPI,
```
EXPHome=/experiment/<dataset>/pos_world/
cd $EXPHome
./comp_run_collection.sh <qId>
```
This will generate two files, pw_<qID> and happi_<qId>, for query <qId> corresponding to PosWorld and HaPPI. 


2. The time taken by HaPPI to incrementally maintain the answers of query qID under edge isnertion operation over 10 runs,
```
EXPHome:/experiment/<dataset>/maintenance/insertion/
cd $EXPHome
./mat_run_collector.sh <qId>
```
Note that for each query answer set, the raw computation and maintenance time over 10 runs can be found respective EXPHome.

## License

HaPPI is provided as open-source software under the MIT License. See [LICENSE](LICENSE).

## Contact

https://github.com/gaurgarima/HaPPI

Garima Gaur <garimag@cse.iitk.ac.in>
