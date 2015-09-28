(function() {
  'use strict';

  angular
    .module('ui')
    .run(runBlock);

  /** @ngInject */
  function runBlock($log) {

    $log.debug('runBlock end');
  }

})();
