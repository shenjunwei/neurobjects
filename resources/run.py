#!/usr/bin/env python
# -*- coding: utf-8 -*-


import os
import sys


def get_paths(data_dir, animal):
  config_dir = os.path.join(data_dir, animal)
  config_path = os.path.join(config_dir, animal + '_setup.yml')
  evaluator_path = os.path.join(config_dir, 'evaluator.yml')
  datasets_path = os.path.join(config_dir, 'datasets')
  
  return config_path, evaluator_path, datasets_path


def clean(data_dir, animal):
  print 'Cleaning datasets for', animal
  
  __, _, datasets_path = get_paths(data_dir, animal)
  os.system('rm %s -rf' % (datasets_path,))
  os.system('mkdir -p %s' % (datasets_path,))


def generate(data_dir, animal):
  print 'Generate datasets for', animal

  config_path, _, _ = get_paths(data_dir, animal)
  os.system('java -Xmx1500m -jar ~/workspace/neurobjects/dist/dataset-generator.jar -v ' + config_path)


def evaluate(data_dir, animal):
  print 'Evaluating datasets for', animal
 
  config_path, evaluator_path, _ = get_paths(data_dir, animal)
  os.system('java -Xmx1500m -jar ~/workspace/neurobjects/dist/dataset-evaluator.jar -v ' +
            config_path + ' ' + evaluator_path)

def fast_eval(data_dir, animal):
  print 'Evaluating datasets for', animal, '(fast mode)'

  config_path, evaluator_path, _ = get_paths(data_dir, animal)
  os.system('java -Xmx 1500m -jar ~/workspace/neurobjects/dist/fast-evaluator.jar -v ' +
            config_path + ' ' + evaluator_path)


if __name__ == '__main__':
  command = sys.argv[1]
  data_dir = sys.argv[2]

  for animal in sys.argv[3:]:
    if command == 'clean':
      clean(data_dir, animal)
    elif command == 'generate':
      generate(data_dir, animal)
    elif command == 'evaluate':
      evaluate(data_dir, animal)
    elif command == 'fast_eval':
      fast_eval(data_dir, animal)

