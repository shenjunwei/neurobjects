# How to add a new surrogate #
**TODO: completar o texto e colocar em inglês**

  1. Tratar o setup file
    * GeneratorSetup
    * GeneratorSetupTest
  1. Colocar o seu surrogado no DatasetTransformer
    * Fazer um needsBehaviorHandlerTransform
  1. Se necessario, adaptar o DatasetGenerator para fazer transformações no BehaviorHandler
    * Escreve os testes no DatasetGeneratorTest